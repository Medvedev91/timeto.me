import SwiftUI
import shared

struct RepeatingFormSheet: View {
    
    let initRepeatingDb: RepeatingDb?
    
    var body: some View {
        VmView({
            RepeatingFormVm(
                initRepeatingDb: initRepeatingDb
            )
        }) { vm, state in
            RepeatingFormSheetInner(
                vm: vm,
                state: state,
                text: state.text
            )
        }
    }
}

private struct RepeatingFormSheetInner: View {
    
    let vm: RepeatingFormVm
    let state: RepeatingFormVm.State
    
    @State var text: String
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        Form {
            Section {
                TextField(
                    state.textPlaceholder,
                    text: $text
                )
                .onChange(of: text) { _, newText in
                    vm.setText(newText: newText)
                }
            }
        }
        .myFormContentMargins()
        .navigationTitle(state.title)
        .toolbarTitleDisplayMode(.inline)
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
                .fontWeight(.semibold)
            }
        }
    }
}
