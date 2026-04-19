import SwiftUI
import shared

struct TaskFolderFormSheet: View {
    
    let taskFolderDb: TaskFolderDb?
    
    var body: some View {
        VmView({
            TaskFolderFormVm(
                folderDb: taskFolderDb,
            )
        }) { vm, state in
            TaskFolderFormSheetInner(
                vm: vm,
                state: state,
                name: state.name,
                activityDb: state.activityDb,
            )
        }
    }
}

private struct TaskFolderFormSheetInner: View {
    
    let vm: TaskFolderFormVm
    let state: TaskFolderFormVm.State
    
    @State var name: String
    @State var activityDb: ActivityDb?

    ///
    
    @FocusState private var isFocused: Bool
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        List {
            
            Section {
                TextField(
                    state.namePlaceholder,
                    text: $name
                )
                .focused($isFocused)
                .onChange(of: name) { _, new in
                    vm.setName(name: new)
                }
            }
            
            if state.isActivityAvailable {
                Section {
                    Picker(state.activityTitle, selection: $activityDb) {
                        if activityDb == nil {
                            Text("None")
                                .tag(nil as ActivityDb?) // Support optional (nil) selection
                        }
                        ForEach(state.activitiesUi, id: \.activityDb) { activityUi in
                            Text(activityUi.title)
                                .tag(activityUi.activityDb as ActivityDb?) // Support optional (nil) selection
                        }
                    }
                    .pickerStyle(.menu)
                    .accentColor(.secondary)
                    .foregroundColor(.primary)
                    .onChange(of: activityDb) { _, activityDb in
                        vm.setActivity(activityDb: activityDb)
                    }
                }
            }
            
            if let folderDb = state.folderDb {
                Section {
                    Button(state.deleteText) {
                        vm.delete(
                            folderDb: folderDb,
                            dialogsManager: navigation,
                            onDelete: {
                                dismiss()
                            }
                        )
                    }
                    .foregroundColor(.red)
                }
            }
        }
        .myFormContentMargins()
        .interactiveDismissDisabled()
        .toolbarTitleDisplayMode(.inline)
        .navigationTitle(state.title)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button(state.doneText) {
                    vm.save(
                        dialogsManager: navigation,
                        onSuccess: {
                            dismiss()
                        }
                    )
                }
                .fontWeight(.bold)
                .disabled(!state.isSaveEnabled)
            }
        }
        .onAppear {
            isFocused = false
        }
    }
}
