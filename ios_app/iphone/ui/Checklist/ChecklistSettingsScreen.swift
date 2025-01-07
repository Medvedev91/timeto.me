import SwiftUI
import shared

struct ChecklistSettingsScreen: View {
    
    let checklistDb: ChecklistDb?
    let onSave: (ChecklistDb) -> Void
    
    var body: some View {
        
        VmView({
            ChecklistFormSettingsVm(checklistDb: checklistDb)
        }) { vm, state in
            ChecklistSettingsScreenInner(
                vm: vm,
                state: state,
                name: state.name,
                onSave: onSave
            )
        }
    }
}

private struct ChecklistSettingsScreenInner: View {
    
    let vm: ChecklistFormSettingsVm
    let state: ChecklistFormSettingsVm.State
    @State var name: String
    let onSave: (ChecklistDb) -> Void
    
    ///
    
    @FocusState private var isFocused: Bool
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        List {
            TextField(
                text: $name,
                prompt: Text(state.namePlaceholder)
            ) {
            }
            .focused($isFocused)
            .onChange(of: name) { _, new in
                vm.setName(name: new)
            }
        }
        .contentMargins(.top, 14)
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
                Button(state.saveButtonText) {
                    vm.save(
                        dialogsManager: navigation,
                        onSuccess: { newChecklistDb in
                            dismiss()
                            onSave(newChecklistDb)
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
