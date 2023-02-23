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

    @State private var isFoldersSettingsPresented = false
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

                VStack(spacing: 0) {

                    VStack(spacing: 0) {

                        MyListView__Padding__SectionHeader()

                        MyListView__HeaderView(
                                title: "CHECKLISTS",
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

                        MyListView__Padding__HeaderSection()

                        let checklists = state.checklists
                        ForEach(checklists, id: \.id) { checklist in
                            let isFirst = checklists.first == checklist
                            MyListView__ItemView(
                                    isFirst: isFirst,
                                    isLast: checklists.last == checklist,
                                    withTopDivider: !isFirst
                            ) {
                                ToolsView_ChecklistView(checklist: checklist)
                            }
                        }
                    }

                    ///
                    /// Shortcuts

                    VStack(spacing: 0) {

                        MyListView__Padding__SectionHeader()

                        MyListView__HeaderView(
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

                        MyListView__Padding__HeaderSection()

                        let shortcuts = state.shortcuts
                        ForEach(shortcuts, id: \.id) { shortcut in
                            let isFirst = shortcuts.first == shortcut
                            MyListView__ItemView(
                                    isFirst: isFirst,
                                    isLast: shortcuts.last == shortcut,
                                    withTopDivider: !isFirst
                            ) {
                                ToolsView_ShortcutView(shortcut: shortcut)
                            }
                        }
                    }

                    ///
                    /// Settings

                    VStack(spacing: 0) {

                        MyListView__Padding__SectionHeader()

                        MyListView__HeaderView(title: "SETTINGS")

                        MyListView__Padding__HeaderSection()

                        MyListView__ItemView(
                                isFirst: true,
                                isLast: false
                        ) {
                            MyListView__ItemView__ButtonView(
                                    text: "Folders",
                                    withArrow: true
                            ) {
                                isFoldersSettingsPresented = true
                            }
                                    .sheetEnv(isPresented: $isFoldersSettingsPresented) {
                                        FoldersSettingsSheet(isPresented: $isFoldersSettingsPresented)
                                    }
                        }

                        MyListView__ItemView(
                                isFirst: false,
                                isLast: true,
                                withTopDivider: true
                        ) {
                            MyListView__ItemView__ButtonView(
                                    text: "Day Start",
                                    rightView: AnyView(
                                            Text(state.dayStartNote)
                                                    .foregroundColor(.secondary)
                                                    .font(.system(size: 15))
                                                    .padding(.trailing, MyListView.PADDING_INNER_HORIZONTAL)
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
                    }

                    ///
                    /// Backup

                    VStack(spacing: 0) {

                        MyListView__Padding__SectionHeader()

                        MyListView__HeaderView(title: "BACKUPS")

                        MyListView__Padding__HeaderSection()

                        MyListView__ItemView(
                                isFirst: true,
                                isLast: false
                        ) {

                            MyListView__ItemView__ButtonView(text: "Create") {
                                Task {
                                    let jString = try await Backup.shared.create(type: "manual", intervalsLimit: 999_999_999.toInt32()) // todo

                                    let formatter = DateFormatter()
                                    formatter.dateFormat = "yyyyMMdd_HHmmss"
                                    fileForExportName = "timeto_\(formatter.string(from: Date())).json"

                                    fileForExport = MyJsonFileDocument(initialText: jString)
                                    isFileExporterPresented = true
                                }
                            }
                        }

                        MyListView__ItemView(
                                isFirst: false,
                                isLast: false,
                                withTopDivider: true
                        ) {

                            MyListView__ItemView__ButtonView(text: "Restore") {
                                isFileImporterPresented = true
                            }
                        }

                        MyListView__ItemView(
                                isFirst: false,
                                isLast: true,
                                withTopDivider: true
                        ) {

                            let autoBackupString: String = {
                                guard let lastBackupDate = autoBackup.lastDate else {
                                    return "None"
                                }
                                let formatter = DateFormatter()
                                formatter.dateFormat = is12HoursFormat() ? "d MMM, hh:mm a" : "d MMM, HH:mm"
                                return formatter.string(from: lastBackupDate)
                            }()
                            MyListView__ItemView__ButtonView(
                                    text: "Auto Backup",
                                    rightView: AnyView(
                                            Text(autoBackupString)
                                                    .foregroundColor(.secondary)
                                                    .font(.system(size: 15))
                                                    .padding(.trailing, MyListView.PADDING_INNER_HORIZONTAL)
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

                    MyListView__Padding__SectionHeader()

                    MyListView__ItemView(
                            isFirst: true,
                            isLast: false
                    ) {
                        MyListView__ItemView__ButtonView(text: "Ask a Question") {
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

                    MyListView__ItemView(
                            isFirst: false,
                            isLast: true,
                            withTopDivider: true
                    ) {

                        MyListView__ItemView__ButtonView(text: "Open Source") {
                            UIApplication.shared.open(URL(string: state.openSourceUrl)!)
                        }
                    }
                }

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

    @State private var isItemsPresented = false
    @State private var isEditPresented = false

    var body: some View {
        MyListSwipeToActionItem(
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
                    .padding(.horizontal, MyListView.PADDING_INNER_HORIZONTAL)
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

    @State private var isEditPresented = false

    var body: some View {
        MyListSwipeToActionItem(
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
                    .padding(.horizontal, MyListView.PADDING_INNER_HORIZONTAL)
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
                        UtilsKt.showUiAlert(message: error, reportApiText: nil)
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
