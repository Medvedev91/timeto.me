import SwiftUI
import shared

struct ChecklistItemFormSheet: View {
    
    let checklistDb: ChecklistDb
    let checklistItemDb: ChecklistItemDb?
    
    var body: some View {
        VmView({
            ChecklistItemFormVm(
                checklistDb: checklistDb,
                checklistItemDb: checklistItemDb
            )
        }) { vm, state in
            ChecklistItemFormSheetInner(
                vm: vm,
                state: state,
                text: state.text
            )
        }
    }
}

///

private struct ChecklistItemFormSheetInner: View {
    
    let vm: ChecklistItemFormVm
    let state: ChecklistItemFormVm.State
    
    @State var text: String
    
    ///
    
    @FocusState private var isFocused: Bool
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        List {
            
            TextField(
                text: $text
            ) {
            }
            .focused($isFocused)
            .onChange(of: text) { _, new in
                vm.setText(text: new)
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
                Button(state.saveButtonText) {
                    vm.save(
                        dialogsManager: navigation,
                        onSuccess: {
                            dismiss()
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
