import SwiftUI
import shared

struct TaskFolderFormSheet: View {
    
    let taskFolderDb: TaskFolderDb?
    
    var body: some View {
        VmView({
            TaskFolderFormVm(
                folderDb: taskFolderDb
            )
        }) { vm, state in
            TaskFolderFormSheetInner(
                vm: vm,
                state: state,
                name: state.name
            )
        }
    }
}

private struct TaskFolderFormSheetInner: View {
    
    let vm: TaskFolderFormVm
    let state: TaskFolderFormVm.State
    
    @State var name: String
    
    ///
    
    @FocusState private var isFocused: Bool
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        List {
            
            TextField(
                state.namePlaceholder,
                text: $name
            )
            .focused($isFocused)
            .onChange(of: name) { _, new in
                vm.setName(name: new)
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
                Button(state.saveText) {
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
            isFocused = true
        }
    }
}
