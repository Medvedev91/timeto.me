import SwiftUI
import UniformTypeIdentifiers
import shared

struct SettingsScreen: View {
    
    var body: some View {
        VmView({
            SettingsSheetVm()
        }) { vm, state in
            SettingsScreenInner(
                vm: vm,
                state: state
            )
        }
    }
}

private struct SettingsScreenInner: View {
    
    let vm: SettingsSheetVm
    let state: SettingsSheetVm.State
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    // todo

    @State private var isFileImporterPresented = false

    // todo remove
    @State private var isFoldersSettingsPresented = false
    // todo remove
    @State private var isDayStartPresented = false

    @State private var isFileExporterPresented = false
    @State private var fileForExport: MyJsonFileDocument? = nil
    @State private var fileForExportName: String? = nil

    // todo remove
    @State private var isAddChecklistPresented = false
    // todo remove
    @State private var selectedFlavor = "c"

    // todo remove
    @State private var isAddShortcutPresented = false
    // todo remove
    @State private var tmp = false

    var body: some View {
        
        List {
            
            Section {
                
                NavigationLink(.readme(defaultItem: .basics)) {
                    Text(state.readmeTitle)
                }
                
                NavigationLink(.whatsNew) {
                    HStack {
                        Text(state.whatsNewTitle)
                        Spacer()
                        Text(state.whatsNewNote)
                            .foregroundColor(.secondary)
                    }
                }
            }
            
            Section("CHECKLISTS") {
            }

            ///
            
            Section("aill") {
                
                NavigationLink(.whatsNew) {
                    Text("Edit Username")
                }
                
                Toggle(
                    isOn: $tmp,
                    label: { Text("ttess") }
                )
                
                Picker("Fll", selection: $selectedFlavor) {
                    Text("Chocolate").tag("c")
                    Text("Vanilla").tag("v")
                    Text("Strawberry").tag("s")
                }
            }
            
            Section {                        Text("d")                    }
            Section {                        Text("d")                    }
            Section {                        Text("d")                    }
            Section {                        Text("d")                    }
            Section {                        Text("d")                    }
            Section {                        Text("d")                    }
            Section {                        Text("d")                    }
            Section {                        Text("d")                    }
            
            /*
             VStack {
             
             
             ///
             /// Checklists
             
             VStack {
             
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
             
             VStack {
             
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
             /// Shortcuts
             
             VStack {
             
             MyListView__Padding__SectionHeader()
             
             MyListView__HeaderView(
             title: "NOTES",
             rightView: AnyView(
             Button(
             action: {
             nativeSheet.show { isPresented in
             NoteFormSheet(
             isPresented: isPresented,
             note: nil,
             onDelete: {}
             )
             }
             },
             label: {
             Image(systemName: "plus")
             }
             )
             )
             )
             
             MyListView__Padding__HeaderSection()
             
             let notes = state.notes
             ForEach(notes, id: \.id) { note in
             let isFirst = notes.first == note
             MyListView__ItemView(
             isFirst: isFirst,
             isLast: notes.last == note,
             bgColor: c.fg,
             withTopDivider: !isFirst
             ) {
             NoteListItemView(note: note)
             }
             }
             }
             
             ///
             /// Settings
             
             VStack {
             
             MyListView__Padding__SectionHeader()
             
             MyListView__HeaderView(title: "SETTINGS")
             
             MyListView__Padding__HeaderSection()
             
             MyListView__ItemView(
             isFirst: true,
             isLast: false,
             bgColor: c.fg
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
             isLast: false,
             bgColor: c.fg,
             withTopDivider: true
             ) {
             MyListView__ItemView__ButtonView(
             text: "Day Start",
             rightView: AnyView(
             MyListView__ItemView__ButtonView__RightText(
             text: state.dayStartNote
             )
             )
             ) {
             isDayStartPresented = true
             }
             .sheetEnv(isPresented: $isDayStartPresented) {
             DayStartDialog(
             isPresented: $isDayStartPresented,
             settingsSheetVM: vm,
             settingsSheetState: state
             )
             }
             }
             
             MyListView__ItemView(
             isFirst: false,
             isLast: true,
             bgColor: c.fg,
             withTopDivider: true
             ) {
             
             MyListView__ItemView__SwitchView(
             text: state.todayOnHomeScreenText,
             isActive: state.todayOnHomeScreen
             ) {
             vm.toggleTodayOnHomeScreen()
             }
             }
             }
             
             ///
             /// Backup
             
             VStack {
             
             MyListView__Padding__SectionHeader()
             
             MyListView__HeaderView(title: "BACKUPS")
             
             MyListView__Padding__HeaderSection()
             
             MyListView__ItemView(
             isFirst: true,
             isLast: false,
             bgColor: c.fg
             ) {
             
             MyListView__ItemView__ButtonView(text: "Create") {
             Task {
             let jString = try await Backup.shared.create(type: "manual", intervalsLimit: 999_999_999.toInt32()) // todo
             fileForExportName = vm.prepBackupFileName()
             fileForExport = MyJsonFileDocument(initialText: jString)
             isFileExporterPresented = true
             }
             }
             }
             
             MyListView__ItemView(
             isFirst: false,
             isLast: false,
             bgColor: c.fg,
             withTopDivider: true
             ) {
             
             MyListView__ItemView__ButtonView(text: "Restore") {
             isFileImporterPresented = true
             }
             }
             
             MyListView__ItemView(
             isFirst: false,
             isLast: true,
             bgColor: c.fg,
             withTopDivider: true
             ) {
             
             MyListView__ItemView__ButtonView(
             text: "Auto Backup",
             rightView: AnyView(
             MyListView__ItemView__ButtonView__RightText(
             text: state.autoBackupTimeString
             )
             )
             ) {
             // todo do catch
             // https://stackoverflow.com/a/64592118/5169420
             let path = try! AutoBackupIos.autoBackupsFolder()
             .absoluteString
             .replacingOccurrences(of: "file://", with: "shareddocuments://")
             UIApplication.shared.open(URL(string: path)!)
             }
             }
             }
             
             
             ///
             /// Mics
             
             MyListView__Padding__SectionHeader()
             
             AskAQuestionButtonView(
             subject: state.feedbackSubject,
             isFirst: true,
             isLast: false,
             bgColor: c.fg,
             withTopDivider: false
             )
             
             MyListView__ItemView(
             isFirst: false,
             isLast: false,
             bgColor: c.fg,
             withTopDivider: true
             ) {
             
             MyListView__ItemView__ButtonView(text: "Open Source") {
             showOpenSource()
             }
             }
             
             MyListView__ItemView(
             isFirst: false,
             isLast: true,
             bgColor: c.fg,
             withTopDivider: true
             ) {
             
             MyListView__ItemView__ButtonView(
             text: "Privacy",
             rightView: AnyView(
             HStack {
             if let privacyNote = state.privacyNote {
             Text(privacyNote)
             .font(.system(size: 21))
             .padding(.trailing, H_PADDING)
             } else {
             Text("")
             }
             }
             )
             ) {
             nativeSheet.show { isPresented in
             PrivacySheet(
             isPresented: isPresented
             )
             }
             }
             }
             }
             
             HStack {
             Text("timeto.me for iOS\nv\(Bundle.main.infoDictionary!["CFBundleShortVersionString"] as! String).\(state.appVersion)")
             .foregroundColor(.secondary)
             .font(.system(size: 16))
             .multilineTextAlignment(.center)
             }
             .padding(.top, 24)
             .padding(.bottom, 34)
             */
        }
        .contentMargins(.top, 14)
        .toolbarTitleDisplayMode(.inline)
        .navigationTitle(state.headerTitle)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button("Back") {
                    dismiss()
                }
            }
        }
        .sheetEnv(isPresented: $isAddChecklistPresented) {
            ChecklistNameDialog(isPresented: $isAddChecklistPresented, checklist: nil, onSave: { _ in })
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
            do {
                let fileUrl = try res.get()
                
                ///
                /// https://stackoverflow.com/a/64351217
                if !fileUrl.startAccessingSecurityScopedResource() {
                    throw MyError("iOS restore !fileUrl.startAccessingSecurityScopedResource()")
                }
                guard let jString = String(data: try Data(contentsOf: fileUrl), encoding: .utf8) else {
                    throw MyError("iOS restore jString null")
                }
                fileUrl.stopAccessingSecurityScopedResource()
                //////
                
                vm.procRestore(jString: jString)
            } catch {
                Utils_kmpKt.showUiAlert(message: "Error", reportApiText: "iOS restore exception\n" + error.myMessage())
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
        private let settingsSheetVM: SettingsSheetVm
        private let settingsSheetState: SettingsSheetVm.State

        init(
            isPresented: Binding<Bool>,
            settingsSheetVM: SettingsSheetVm,
            settingsSheetState: SettingsSheetVm.State
        ) {
            _isPresented = isPresented
            self.settingsSheetVM = settingsSheetVM
            self.settingsSheetState = settingsSheetState
            _selectedDayStart = State(initialValue: settingsSheetState.dayStartSeconds)
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
                            settingsSheetVM.upDayStartOffsetSeconds(seconds: selectedDayStart) {
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
                    ForEach(settingsSheetState.dayStartListItems, id: \.seconds) { item in
                        Text(item.note).tag(item.seconds)
                    }
                }
                .pickerStyle(WheelPickerStyle())

                Spacer()
            }
        }
    }
}

/*
// todo rename
struct ToolsView_ChecklistView: View {

    let checklist: ChecklistDb

    @State private var isItemsPresented = false
    @State private var isEditPresented = false

    var body: some View {
        MyListSwipeToActionItem(
            bgColor: c.fg,
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
            MyListView__ItemView__ButtonView(text: checklist.name) {
                isItemsPresented = true
            }
        }
        .sheetEnv(isPresented: $isItemsPresented) {
            ChecklistSheet(isPresented: $isItemsPresented, checklist: checklist)
        }
        .sheetEnv(isPresented: $isEditPresented) {
            ChecklistNameDialog(isPresented: $isEditPresented, checklist: checklist, onSave: { _ in })
        }
    }
}
private struct NoteListItemView: View {

    let note: NoteDb

    @EnvironmentObject private var nativeSheet: NativeSheet

    var body: some View {

        MyListView__ItemView__ButtonView(
            text: note.title,
            maxLines: 1,
            rightView: AnyView(Padding(horizontal: H_PADDING))
        ) {
            nativeSheet.show { isPresented in
                NoteSheet(isPresented: isPresented, initNote: note)
            }
        }
    }
}

// todo rename
struct ToolsView_ShortcutView: View {

    let shortcut: ShortcutDb

    @State private var isEditPresented = false

    var body: some View {
        MyListSwipeToActionItem(
            bgColor: c.fg,
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
            MyListView__ItemView__ButtonView(text: shortcut.name) {
                shortcut.performUI()
            }
        }
        .sheetEnv(isPresented: $isEditPresented) {
            ShortcutFormSheet(isPresented: $isEditPresented, editedShortcut: shortcut)
        }
    }
}

*/
