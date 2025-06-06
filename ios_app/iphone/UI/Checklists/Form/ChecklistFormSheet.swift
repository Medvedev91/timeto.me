import SwiftUI
import shared

struct ChecklistFormSheet: View {
    
    let checklistDb: ChecklistDb?
    let onSave: (ChecklistDb) -> Void
    let onDelete: () -> Void

    var body: some View {
        
        VmView({
            ChecklistFormVm(checklistDb: checklistDb)
        }) { vm, state in
            ChecklistFormSheetInner(
                vm: vm,
                state: state,
                name: state.name,
                onSave: onSave,
                onDelete: onDelete
            )
        }
    }
}

private struct ChecklistFormSheetInner: View {
    
    let vm: ChecklistFormVm
    let state: ChecklistFormVm.State
    
    @State var name: String
    
    let onSave: (ChecklistDb) -> Void
    let onDelete: () -> Void
    
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
            
            if let checklistDb = state.checklistDb {
                Section {
                    Button(state.deleteText) {
                        vm.delete(
                            checklistDb: checklistDb,
                            dialogsManager: navigation,
                            onDelete: {
                                dismiss()
                                onDelete()
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
                        onSuccess: { newChecklistDb in
                            dismiss()
                            onSave(newChecklistDb)
                        }
                    )
                }
                .fontWeight(.semibold)
                .disabled(!state.isSaveEnabled)
            }
        }
        .onAppear {
            isFocused = true
        }
    }
}
