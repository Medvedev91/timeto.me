import SwiftUI
import UniformTypeIdentifiers
import shared

struct SettingsScreen: View {
    
    var body: some View {
        VmView({
            SettingsVm()
        }) { vm, state in
            SettingsScreenInner(
                vm: vm,
                state: state
            )
        }
    }
}

private struct SettingsScreenInner: View {
    
    let vm: SettingsVm
    let state: SettingsVm.State
    
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
    @State private var selectedFlavor = "c"

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
                
                ForEach(state.checklistsDb, id: \.id) { checklistDb in
                    NavigationLink(.checklist(
                        checklistDb: checklistDb,
                        maxLines: 9,
                        onDelete: {
                            navigation.cleanPath()
                        }
                    )) {
                        Text(checklistDb.name)
                    }
                    .contextMenu {
                        Button(
                            action: {
                                navigation.sheet {
                                    ChecklistItemsFormSheet(
                                        checklistDb: checklistDb,
                                        onDelete: {}
                                    )
                                }
                            },
                            label: {
                                Label("Edit", systemImage: "square.and.pencil")
                            }
                        )
                    }
                }
                
                Button("New Checklist") {
                    navigation.sheet {
                        ChecklistFormSheet(
                            checklistDb: nil,
                            onSave: { newChecklistDb in
                                navigation.sheet {
                                    ChecklistItemsFormSheet(
                                        checklistDb: newChecklistDb,
                                        onDelete: {
                                            navigation.cleanPath()
                                        }
                                    )
                                }
                            },
                            onDelete: {
                                navigation.cleanPath()
                            }
                        )
                    }
                }
            }
            
            Section("SHORTCUTS") {
                
                ForEach(state.shortcutsDb, id: \.id) { shortcutDb in
                    
                    Button(shortcutDb.name) {
                        shortcutDb.performUi()
                    }
                    .foregroundColor(.primary)
                    .contextMenu {
                        Button(
                            action: {
                                navigation.sheet {
                                    ShortcutFormSheet(
                                        shortcutDb: shortcutDb,
                                        onSave: { _ in }
                                    )
                                }
                            },
                            label: {
                                Label("Edit", systemImage: "square.and.pencil")
                            }
                        )
                    }
                }
                
                Button("New Shortcut") {
                    navigation.sheet {
                        ShortcutFormSheet(
                            shortcutDb: nil,
                            onSave: { _ in }
                        )
                    }
                }
            }

            ///
            
            
            Section("Backups") {
                
                Button("Restore") {
                    isFileImporterPresented = true
                }
            }

            Section("aill") {
                
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
            
            /*
             VStack {
             
             
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
        .contentMargins(.top, 12)
        .contentMarginsTabBar(extra: 28)
        .toolbarTitleDisplayMode(.inlineLarge)
        .navigationTitle(state.headerTitle)
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
        private let settingsSheetVM: SettingsVm
        private let settingsSheetState: SettingsVm.State

        init(
            isPresented: Binding<Bool>,
            settingsSheetVM: SettingsVm,
            settingsSheetState: SettingsVm.State
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
*/
