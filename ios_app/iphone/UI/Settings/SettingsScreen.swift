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
                state: state,
                todayOnHomeScreen: state.todayOnHomeScreen
            )
        }
    }
}

private struct SettingsScreenInner: View {
    
    let vm: SettingsVm
    let state: SettingsVm.State
    
    @State var todayOnHomeScreen: Bool
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    @State private var isFileImporterPresented = false
    
    @State private var isFileExporterPresented = false
    @State private var fileForExport: MyJsonFileDocument? = nil
    @State private var fileForExportName: String? = nil
    
    var body: some View {
        
        List {
            
            Section {
                
                NavigationLinkFullScreen(
                    label: {
                        Text(state.readmeTitle)
                            .foregroundColor(.primary)
                    },
                    fullScreen: {
                        ReadmeFullScreen(defaultItem: .basics)
                    }
                )
                
                NavigationLinkPush(.whatsNew) {
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
                    NavigationLinkPush(.checklist(
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
                                    ChecklistFormItemsSheet(
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
                                    ChecklistFormItemsSheet(
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
            
            Section("NOTES") {
                
                ForEach(state.notesDb, id: \.id) { noteDb in
                    
                    NavigationLinkPush(.note(
                        noteDb: noteDb,
                        onDelete: {
                            navigation.cleanPath()
                        }
                    )) {
                        Text(noteDb.title)
                    }
                    .contextMenu {
                        Button(
                            action: {
                                navigation.sheet {
                                    NoteFormSheet(
                                        noteDb: noteDb,
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
                
                Button("New Note") {
                    navigation.sheet {
                        NoteFormSheet(
                            noteDb: nil,
                            onDelete: {}
                        )
                    }
                }
            }
            
            Section("SETTINGS") {
                
                NavigationLinkPush(.taskFoldersForm) {
                    Text("Folders")
                }
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text("Day Start")
                                .foregroundColor(.primary)
                            Spacer()
                            Text(state.dayStartNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        SettingsDayStartSheet(
                            vm: vm,
                            state: state,
                            dayStart: state.dayStartSeconds
                        )
                    }
                )
                
                NavigationLinkFullScreen(
                    label: {
                        Text(state.homeScreenText)
                            .foregroundColor(.primary)
                    },
                    fullScreen: {
                        HomeSettingsButtonsFullScreen()
                    }
                )
                
                Toggle(
                    state.todayOnHomeScreenText,
                    isOn: $todayOnHomeScreen
                )
                .onChange(of: todayOnHomeScreen) { _, new in
                    vm.setTodayOnHomeScreen(isOn: new)
                }
            }
            
            Section("Backups") {
                
                Button("Create") {
                    Task {
                        let jString = try await Backup.shared.create(type: "manual", intervalsLimit: 999_999_999.toInt32())
                        fileForExportName = vm.prepBackupFileName()
                        fileForExport = MyJsonFileDocument(initialText: jString)
                        isFileExporterPresented = true
                    }
                }
                .foregroundColor(.primary)
                
                Button("Restore") {
                    isFileImporterPresented = true
                }
                .foregroundColor(.primary)
                
                NavigationLinkAction(
                    label: {
                        HStack {
                            Text("Auto Backup")
                                .foregroundColor(.primary)
                            Spacer()
                            Text(state.autoBackupTimeString)
                                .foregroundColor(.secondary)
                        }
                    },
                    action: {
                        do {
                            // https://stackoverflow.com/a/64592118/5169420
                            let path = try AutoBackupIos.autoBackupsFolder()
                                .absoluteString
                                .replacingOccurrences(of: "file://", with: "shareddocuments://")
                            UIApplication.shared.open(URL(string: path)!)
                        } catch {
                            navigation.alert(message: "Error")
                            reportApi("AutoBackupIos.autoBackupsFolder() error:\n\(error)")
                        }
                    }
                )
            }
            
            Section {
                
                AskQuestionView(
                    subject: state.feedbackSubject,
                    content: {
                        Text("Ask a Question")
                            .foregroundColor(.primary)
                    }
                )
                
                Button("Open Source") {
                    showOpenSource()
                }
                .foregroundColor(.primary)
                
                NavigationLinkPush(.privacy) {
                    HStack {
                        Text("Privacy")
                        Spacer()
                        if let privacyEmoji = state.privacyEmoji {
                            FormButtonEmojiView(emoji: privacyEmoji)
                        }
                    }
                }
            }
            
            Text(state.infoText)
                .customListItem()
                .textAlign(.center)
                .font(.system(size: 16))
                .foregroundColor(.secondary)
                .listRowBackground(Color(.systemBackground))
                .padding(.bottom, 40)
        }
        .contentMargins(.top, 12)
        .contentMarginsTabBar()
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
                print(error.messageApp())
            }
        }
        .fileImporter(
            isPresented: $isFileImporterPresented,
            allowedContentTypes: [.json]
        ) { res in
            do {
                
                //
                // https://stackoverflow.com/a/64351217
                
                let fileUrl = try res.get()
                
                if !fileUrl.startAccessingSecurityScopedResource() {
                    throw ErrorApp("iOS restore !fileUrl.startAccessingSecurityScopedResource()")
                }
                
                guard let jString = String(data: try Data(contentsOf: fileUrl), encoding: .utf8) else {
                    throw ErrorApp("iOS restore jString null")
                }
                
                fileUrl.stopAccessingSecurityScopedResource()
                
                ///
                
                vm.procRestore(jString: jString)
            } catch {
                navigation.alert(message: "Error")
                reportApi("iOS restore exception\n" + error.messageApp())
            }
        }
    }
}

//
// https://www.hackingwithswift.com/quick-start/swiftui/how-to-export-files-using-fileexporter

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
