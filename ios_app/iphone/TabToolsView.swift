import SwiftUI
import UniformTypeIdentifiers
import MessageUI
import shared

struct TabToolsView: View {

    @State private var vm = TabToolsVM()

    @EnvironmentObject private var autoBackup: AutoBackup

    @State private var isReadmePresented = false

    @State private var mailViewResult: Result<MFMailComposeResult, Error>? = nil
    @State private var isMailViewPresented = false

    @State private var isFileImporterPresented = false

    @State private var isDayStartPresented = false

    @State private var isFileExporterPresented = false
    @State private var fileForExport: MyJsonFileDocument? = nil
    @State private var fileForExportName: String? = nil

    @State private var isAddChecklistPresented = false

    @State private var isAddShortcutPresented = false

    //////

    var body: some View {

        VMView(vm: vm, stack: .ZStack()) { state in

            ScrollView(.vertical, showsIndicators: false) {

                ///
                /// Checklists

                MyListHeader(
                        title: "Checklists",
                        rightView: AnyView(
                                Button(
                                        action: {
                                            isAddChecklistPresented.toggle()
                                        },
                                        label: {
                                            Image(systemName: "plus")
                                        }
                                )
                        )
                )
                        .padding(.top, 10)

                MyListSection {
                    let checklists = state.checklists
                    ForEach(checklists, id: \.id) { checklist in
                        ToolsView_ChecklistView(checklist: checklist, withDivider: checklists.first != checklist)
                    }
                }


                ///
                /// Shortcuts

                MyListHeader(
                        title: "SHORTCUTS",
                        rightView: AnyView(
                                Button(
                                        action: {
                                            isAddShortcutPresented.toggle()
                                        },
                                        label: {
                                            Image(systemName: "plus")
                                        }
                                )
                        )
                )

                MyListSection {
                    let shortcuts = state.shortcuts
                    ForEach(shortcuts, id: \.id) { shortcut in
                        ToolsView_ShortcutView(shortcut: shortcut, withDivider: shortcuts.first != shortcut)
                    }
                }

                ///
                /// Settings

                MyListHeader(title: "SETTINGS")

                MyListSection {

                    MyListItem_Button(
                            text: "Day Start",
                            withTopDivider: false,
                            rightView: AnyView(
                                    Text(state.dayStartNote)
                                            .foregroundColor(.secondary)
                                            .font(.system(size: 15))
                                            .padding(.trailing, DEF_LIST_H_PADDING)
                            )
                    ) {
                        isDayStartPresented = true
                    }
                            .sheetEnv(isPresented: $isDayStartPresented) {
                                DayStartDialog(
                                        isPresented: $isDayStartPresented,
                                        tabToolsVM: vm,
                                        tabToolsState: state
                                )
                            }
                }

                ///
                /// Backup

                MyListHeader(title: "Backups")

                MyListSection {

                    MyListItem_Button(text: "Create", withTopDivider: false) {
                        Task {
                            let jString = try await Backup.shared.create(type: "manual", intervalsLimit: 999_999_999.toInt32()) // todo

                            let formatter = DateFormatter()
                            formatter.dateFormat = "yyyyMMdd_HHmmss"
                            fileForExportName = "timeto_\(formatter.string(from: Date())).json"

                            fileForExport = MyJsonFileDocument(initialText: jString)
                            isFileExporterPresented = true
                        }
                    }

                    MyListItem_Button(text: "Restore", withTopDivider: true) {
                        isFileImporterPresented = true
                    }

                    let autoBackupString: String = {
                        guard let lastBackupDate = autoBackup.lastDate else {
                            return "None"
                        }
                        let formatter = DateFormatter()
                        formatter.dateFormat = is12HoursFormat() ? "d MMM, hh:mm a" : "d MMM, HH:mm"
                        return formatter.string(from: lastBackupDate)
                    }()
                    MyListItem_Button(
                            text: "Auto Backup",
                            withTopDivider: true,
                            rightView: AnyView(
                                    Text(autoBackupString)
                                            .foregroundColor(.secondary)
                                            .font(.system(size: 15))
                                            .padding(.trailing, DEF_LIST_H_PADDING)
                            )
                    ) {
                        // todo do catch
                        // https://stackoverflow.com/a/64592118/5169420
                        let path = try! AutoBackup.autoBackupsFolder()
                                .absoluteString
                                .replacingOccurrences(of: "file://", with: "shareddocuments://")
                        UIApplication.shared.open(URL(string: path)!)
                    }
                }


                ///
                /// Mics

                /*
                    Button("Readme") {
                        isReadmePresented = true
                    }
                            .sheetEnv(isPresented: $isReadmePresented) {
                                TabReadmeView(isPresented: $isReadmePresented)
                            }
                            .foregroundColor(.primary)
                     */

                MyListSection {
                    MyListItem_Button(text: "Ask a Question", withTopDivider: false) {
                        if (MFMailComposeViewController.canSendMail()) {
                            isMailViewPresented.toggle()
                        } else {
                            // Взято из skorolek
                            let subjectEncoded = state.feedbackSubject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)!
                            let url = URL(string: "mailto:\(state.feedbackEmail)?subject=\(subjectEncoded)")!
                            UIApplication.shared.open(url)
                        }
                    }
                            .sheetEnv(isPresented: $isMailViewPresented) {
                                MailView(
                                        toEmail: state.feedbackEmail,
                                        subject: state.feedbackSubject,
                                        body: nil,
                                        result: $mailViewResult
                                )
                            }
                }
                        .padding(.top, 10)

                HStack {
                    Text("TimeTo for iOS v" + state.appVersion)
                            .foregroundColor(.secondary)
                            .font(.system(size: 15))
                }
                        .padding(.top, 24)
                        .padding(.bottom, 34)
            }
        }
                .background(Color(.myBackground))
                .sheetEnv(isPresented: $isAddChecklistPresented) {
                    ChecklistFormSheet(isPresented: $isAddChecklistPresented, checklist: nil)
                }
                .sheetEnv(isPresented: $isAddShortcutPresented) {
                    ShortcutFormSheet(isPresented: $isAddShortcutPresented, editedShortcut: nil)
                }
                .onAppear {
                    // todo auto backups https://www.hackingwithswift.com/books/ios-swiftui/writing-data-to-the-documents-directory
                    // let str = "Test Message"
                    // let dd = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
                    // let url = dd.appendingPathComponent("message.txt")
                    // do {
                    // try str.write(to: url, atomically: true, encoding: .utf8)
                    // let input = try String(contentsOf: url)
                    // print(url)
                    // } catch {
                    // print(error.localizedDescription)
                    // }
                }
                .fileExporter(
                        isPresented: $isFileExporterPresented,
                        document: fileForExport,
                        // I do not know why, but I have to set contentType.
                        // It also set in MyJsonFileDocument.
                        contentType: .json,
                        defaultFilename: fileForExportName
                ) { result in
                    switch result {
                    case .success(let url):
                        // todo
                        print("Saved to \(url.absoluteString.removingPercentEncoding!)")
                    case .failure(let error):
                        // todo
                        print(error.myMessage())
                    }
                }
                .fileImporter(isPresented: $isFileImporterPresented, allowedContentTypes: [.json]) { res in
                    /// todo handling errors logic.
                    do {
                        let fileUrl = try res.get()

                        ///
                        /// https://stackoverflow.com/a/64351217
                        if !fileUrl.startAccessingSecurityScopedResource() {
                            fatalError() // todo
                        }
                        let jString = String(data: try! Data(contentsOf: fileUrl), encoding: .utf8)!
                        fileUrl.stopAccessingSecurityScopedResource()
                        //////

                        // todo is it actual?
                        TabsView.lastInstance!.loadingView = AnyView(Text("Loading"))
                        Task {
                            do {
                                try await Backup.shared.restore(jString: jString)
                                // todo I do not know how to restart the app
                                TabsView.lastInstance!.loadingView = AnyView(Text("Please restart the app"))
                            } catch {
                                // todo UI error
                                reportApi("iOS restore exception\n" + error.myMessage())
                            }
                        }
                        //////
                    } catch {
                        // todo message and log
                        print(error.myMessage())
                    }
                }
    }

    /// https://www.hackingwithswift.com/quick-start/swiftui/how-to-export-files-using-fileexporter
    private struct MyJsonFileDocument: FileDocument {

        // Otherwise .txt file
        static var readableContentTypes = [UTType.json]

        var text = ""

        init(initialText: String = "") {
            text = initialText
        }

        init(configuration: ReadConfiguration) throws {
            if let data = configuration.file.regularFileContents {
                text = String(decoding: data, as: UTF8.self)
            }
        }

        func fileWrapper(configuration: WriteConfiguration) throws -> FileWrapper {
            let data = Data(text.utf8)
            return FileWrapper(regularFileWithContents: data)
        }
    }

    private struct DayStartDialog: View {

        @Binding private var isPresented: Bool
        @State private var selectedDayStart: Int32 // WARNING Int32!
        private let tabToolsVM: TabToolsVM
        private let tabToolsState: TabToolsVM.State

        init(
                isPresented: Binding<Bool>,
                tabToolsVM: TabToolsVM,
                tabToolsState: TabToolsVM.State
        ) {
            _isPresented = isPresented
            self.tabToolsVM = tabToolsVM
            self.tabToolsState = tabToolsState
            _selectedDayStart = State(initialValue: tabToolsState.dayStartSeconds)
        }

        var body: some View {

            VStack {

                HStack {

                    Button(
                            action: { isPresented.toggle() },
                            label: { Text("Cancel") }
                    )
                            .padding(.leading, 25)

                    Spacer()

                    Button(
                            action: {
                                tabToolsVM.upDayStartOffsetSeconds(seconds: selectedDayStart) {
                                    isPresented = false
                                }
                            },
                            label: {
                                Text("Save")
                                        .fontWeight(.heavy)
                                        .padding(.trailing, 25)
                            }
                    )
                }
                        .padding(.top, 20)

                Spacer()

                Picker(
                        "",
                        selection: $selectedDayStart
                ) {
                    ForEach(tabToolsState.dayStartListItems, id: \.seconds) { item in
                        Text(item.note).tag(item.seconds)
                    }
                }
                        .pickerStyle(WheelPickerStyle())

                Spacer()
            }
        }
    }
}

struct ToolsView_ChecklistView: View {

    let checklist: ChecklistModel
    let withDivider: Bool

    @State private var isItemsPresented = false
    @State private var isEditPresented = false

    var body: some View {
        MyListSwipeToActionItem(
                withTopDivider: withDivider,
                deletionHint: checklist.name,
                deletionConfirmationNote: "Are you sure you want to delete \"\(checklist.name)\" checklist?",
                onEdit: {
                    isEditPresented = true
                },
                onDelete: {
                    checklist.deleteWithDependencies { _ in
                        // todo
                    }
                }
        ) {
            AnyView(safeView)
                    .padding(.horizontal, DEF_LIST_H_PADDING)
                    .padding(.vertical, DEF_LIST_V_PADDING)
        }
                .sheetEnv(isPresented: $isEditPresented) {
                    ChecklistFormSheet(isPresented: $isEditPresented, checklist: checklist)
                }
    }

    private var safeView: some View {
        Button(
                action: {
                    isItemsPresented = true
                },
                label: {
                    HStack {
                        Text(checklist.name)
                        Spacer()
                    }
                }
        )
                .foregroundColor(.primary)
                .sheetEnv(isPresented: $isItemsPresented) {
                    ChecklistDialog(isPresented: $isItemsPresented, checklist: checklist)
                }
    }
}

struct ToolsView_ShortcutView: View {

    let shortcut: ShortcutModel
    let withDivider: Bool

    @State private var isEditPresented = false
    @EnvironmentObject private var timetoAlert: TimetoAlert

    var body: some View {
        MyListSwipeToActionItem(
                withTopDivider: withDivider,
                deletionHint: shortcut.name,
                deletionConfirmationNote: "Are you sure you want to delete \"\(shortcut.name)\" shortcut?",
                onEdit: {
                    isEditPresented = true
                },
                onDelete: {
                    shortcut.delete { err in
                        // todo report
                    }
                }
        ) {
            AnyView(itemView)
                    .padding(.horizontal, DEF_LIST_H_PADDING)
                    .padding(.vertical, DEF_LIST_V_PADDING)
        }
                .sheetEnv(isPresented: $isEditPresented) {
                    ShortcutFormSheet(isPresented: $isEditPresented, editedShortcut: shortcut)
                }
    }

    private var itemView: some View {
        Button(
                action: {
                    performShortcutOrError(shortcut) { error in
                        timetoAlert.alert(error)
                    }
                },
                label: {
                    HStack {
                        Text(shortcut.name)
                        Spacer()
                    }
                }
        )
                .foregroundColor(.primary)
    }
}
