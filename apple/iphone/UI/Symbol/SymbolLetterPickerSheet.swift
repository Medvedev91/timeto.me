import SwiftUI
import shared

struct SymbolLetterPickerSheet: View {
    
    let onPick: (Symbol.Letter) -> Void
    
    ///
    
    @State var text: String = ""
    
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
        }
        .myFormContentMargins()
        .interactiveDismissDisabled()
        .toolbarTitleDisplayMode(.inline)
        .navigationTitle("Symbol")
        .toolbar {
            
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                Button("Done") {
                    SymbolLetterPickerUtils.shared.validateLetter(
                        letter: text,
                        dialogsManager: navigation,
                        onSuccess: { symbolLetter in
                            onPick(symbolLetter)
                            dismiss()
                        },
                    )
                }
                .fontWeight(.semibold)
            }
        }
        .onAppear {
            isFocused = true
        }
    }
}
