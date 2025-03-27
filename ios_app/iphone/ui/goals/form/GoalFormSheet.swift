import SwiftUI
import shared

struct GoalFormSheet: View {
    
    let strategy: GoalFormStrategy
    
    var body: some View {
        VmView({
            GoalFormVm(
                strategy: strategy
            )
        }) { vm, state in
            GoalFormSheetInner(
                vm: vm,
                state: state,
                strategy: strategy,
                note: state.note
            )
        }
    }
}

private struct GoalFormSheetInner: View {
    
    let vm: GoalFormVm
    let state: GoalFormVm.State
    let strategy: GoalFormStrategy
    
    @State var note: String
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation

    var body: some View {
        List {
            
            Section {
                
                TextField(
                    state.notePlaceholder,
                    text: $note
                )
                .onChange(of: note) { _, new in
                    vm.setNote(newNote: new)
                }
            }
        }
        .navigationTitle(state.title)
        .toolbarTitleDisplayMode(.inline)
        .myFormContentMargins()
        .interactiveDismissDisabled()
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button(state.doneText) {
                    if let formDataStrategy = strategy as? GoalFormStrategy.FormData {
                        guard let formData: GoalFormData = state.buildFormDataOrNull(
                            dialogsManager: navigation,
                            goalDb: formDataStrategy.initGoalFormData?.goalDb
                        ) else { return }
                        formDataStrategy.onDone(formData)
                        dismiss()
                    } else {
                        fatalError()
                    }
                }
                .fontWeight(.semibold)
            }
        }
    }
}
