import Foundation
import SwiftUI
import shared

extension View {
    
    func attachAutoBackupIos() -> some View {
        modifier(AutoBackupIos__Modifier())
    }
}

private struct AutoBackupIos__Modifier: ViewModifier {
    
    @State private var autoBackupTimer = Timer.publish(every: 30, on: .main, in: .common).autoconnect()
    
    func body(content: Content) -> some View {
        content
            .onReceive(autoBackupTimer) { _ in
                AutoBackupIos.dailyBackupIfNeeded()
            }
            .onAppear {
                myAsync {
                    do {
                        AutoBackup.shared.upLastTimeCache(unixTime: try AutoBackupIos.getLastTimeOrNull())
                    } catch {
                        reportApi("AutoBackupIos__Modifier\n\(error)")
                    }
                }
                AutoBackupIos.dailyBackupIfNeeded()
            }
    }
}

///
/// WARNING
/// All operations with backups should be done through this class because of lastDate.
/// You must always update lastDate on changes.
///
class AutoBackupIos {
    
    static func dailyBackupIfNeeded() {
        Task {
            do {
                let lastBackupUnixDay = try getLastTimeOrNull()?.localDay.toInt() ?? 0
                if lastBackupUnixDay < UnixTime(time: time().toInt32(), utcOffset: LocalUtcOffsetKt.localUtcOffset).localDay.toInt() {
                    try await newBackup()
                    try cleanOld()
                }
            } catch {
                reportApi("AutoBackupIos.dailyBackupIfNeeded()\n\(error)")
            }
        }
    }
    
    static func newBackup() async throws {
        let autoBackupData = try await AutoBackup.shared.buildAutoBackup()
        FileManager.default.createFile(
            atPath: try AutoBackupIos.autoBackupsFolder().appendingPathComponent(autoBackupData.fileName).path,
            contents: autoBackupData.jsonString.data(using: .utf8)
        )
        AutoBackup.shared.upLastTimeCache(unixTime: autoBackupData.unixTime)
    }
    
    static func getLastTimeOrNull() throws -> UnixTime? {
        guard let lastBackup = try getAutoBackupsSortedDesc().first?.lastPathComponent else {
            return nil
        }
        return try Backup.shared.fileNameToUnixTime(fileName: lastBackup)
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
    /// Remember about timeto.me-iOS-Info.plist, if I'm right, without NSUbiquitousContainers sync wouldn't work.
    /// Details about plist https://developer.apple.com/forums/thread/23605
    ///
    static private func iCloudContainerUrl() -> URL? {
        // I don't understand the logic of getting the iCloud container, because it doesn't
        // explicitly specify its address "iCloud.me.timeto.app", but somehow it works.
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
