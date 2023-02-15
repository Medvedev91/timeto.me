import Foundation
import SwiftUI
import shared

extension View {

    func attachAutoBackup() -> some View {
        modifier(AutoBackup__Modifier())
    }
}

private struct AutoBackup__Modifier: ViewModifier {

    @StateObject private var autoBackup = AutoBackup()
    @State private var autoBackupTimer = Timer.publish(every: 30, on: .main, in: .common).autoconnect()

    func body(content: Content) -> some View {
        content
                .environmentObject(autoBackup)
                .onReceive(autoBackupTimer) { _ in
                    dailyBackupIfNeeded()
                }
                .onAppear {
                    dailyBackupIfNeeded()
                }
    }

    private func dailyBackupIfNeeded() {
        Task {
            do {
                let lastBackupUnixDay = try AutoBackup.getLastDate()?.toUnixTime().localDay.toInt() ?? 0
                if lastBackupUnixDay < UnixTime(time: time().toInt32(), utcOffset: UtilsKt.localUtcOffset).localDay.toInt() {
                    try await autoBackup.newBackup()
                    try AutoBackup.cleanOld()
                }
            } catch {
                zlog(error) // todo report
            }
        }
    }
}

///
/// WARNING
/// All operations with backups should be done through this class because of lastDate.
/// You must always update lastDate on changes.
///
class AutoBackup: ObservableObject {

    @Published var lastDate: Date?

    init() {
        do {
            lastDate = try AutoBackup.getLastDate()
        } catch {
            zlog(error) // todo report
        }
    }

    func newBackup() async throws {
        let date = Date()
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy_MM_dd__HH_mm_ss" // WARNING Same logic in getLastDate()
        let fileName = "\(formatter.string(from: date)).json"

        let jString = try await Backup.shared.create(type: "autobackup", intervalsLimit: 999_999_999.toInt32()) // todo

        FileManager.default.createFile(
                atPath: try AutoBackup.autoBackupsFolder().appendingPathComponent(fileName).path,
                contents: jString.data(using: .utf8)
        )
        lastDate = date
    }

    static func getLastDate() throws -> Date? {
        guard let lastBackupFileName = try getAutoBackupsSortedDesc().first?.lastPathComponent else {
            return nil
        }
        // Should be like 2022_09_07__19_08_39
        // todo by KMM
        let dateString = String(lastBackupFileName.split(separator: ".").first!)
                .replacingOccurrences(of: "__", with: "_")
        let dateArray = dateString.split(separator: "_")
        if dateArray.count != 6 {
            throw MyError("getLastDate() dateArray.count != 6")
        }
        guard let year = Int(dateArray[0]),
                let month = Int(dateArray[1]),
                let day = Int(dateArray[2]),
                let hour = Int(dateArray[3]),
                let minute = Int(dateArray[4]),
                let second = Int(dateArray[5])
        else {
            throw MyError("getLastDate() dateArray not Int")
        }

        var dc = DateComponents()
        dc.year = year
        dc.month = month
        dc.day = day
        dc.hour = hour
        dc.minute = minute
        dc.second = second

        return Calendar(identifier: .gregorian).date(from: dc)!
    }

    static func cleanOld() throws {
        try getAutoBackupsSortedDesc()
                .dropFirst(10)
                .forEach { url in
                    try FileManager.default.removeItem(at: url)
                }
    }

    static private func getAutoBackupsSortedDesc() throws -> [URL] {
        try FileManager.default.contentsOfDirectory(at: autoBackupsFolder(), includingPropertiesForKeys: nil)
                .filter { $0.path.contains(".json") }
                .sorted { url1, url2 in url1.path > url2.path }
    }

    ///
    /// Folders

    static func autoBackupsFolder() throws -> URL {
        let containerUrl = iCloudContainerUrl() ?? localContainerUrl()
        let autobackupsUrl = containerUrl.appendingPathComponent("autobackups")
        try FileManager.default.createDirectory(at: autobackupsUrl, withIntermediateDirectories: true)
        return autobackupsUrl
    }

    ///
    /// iCloud folder for sync
    ///
    /// Return nil if iCloud sync disabled
    ///
    /// Based on https://youtu.be/fHG6OVAp-5o
    /// Docs: https://developer.apple.com/documentation/uikit/documents_data_and_pasteboard/synchronizing_documents_in_the_icloud_environment
    /// Remember about timeto--iOS--Info.plist, if I'm right without NSUbiquitousContainers sync wouldn't work.
    /// Details about plist https://developer.apple.com/forums/thread/23605
    ///
    static private func iCloudContainerUrl() -> URL? {
        // I don't understand the logic of getting the iCloud container, because it doesn't
        // explicitly specify its address "iCloud.app.time-to.timeto", but somehow it works.
        // Perhaps below "Documents" is the creation of the "Timeto" folder, which is forwarded
        // to iCloud on behalf of the application.
        guard let iCloudContainerUrl = FileManager.default.url(forUbiquityContainerIdentifier: nil) else {
            return nil
        }
        return iCloudContainerUrl.appendingPathComponent("Documents")
    }

    ///
    /// The folder for "Files" -> "On My iPhone".
    /// It is needed to add to plist https://stackoverflow.com/a/49214026/5169420.
    /// But on the build LSSupportsOpeningDocumentsInPlace removes, but added to project.pbxproj.
    ///
    static private func localContainerUrl() -> URL {
        // I don't know why or how many urls are returned, but the examples take [0] everywhere
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
    }
}
