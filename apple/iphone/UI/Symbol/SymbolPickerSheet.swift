import SwiftUI
import shared

struct SymbolPickerSheet: View {
    
    let onPick: (Symbol) -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        List {
            
            Button("Emoji") {
                navigation.sheet {
                    EmojiPickerSheet(
                        onDone: { emoji in
                            onPick(Symbol.Emoji(emoji: emoji))
                            dismiss()
                        },
                    )
                }
            }
            .foregroundColor(.blue)
            .listRowSeparator(.hidden)
            
            Button("Symbol") {
                navigation.sheet {
                    SymbolLetterPickerSheet(
                        onPick: { letter in
                            onPick(letter)
                            dismiss()
                        },
                    )
                }
            }
            .foregroundColor(.blue)
            .listRowSeparator(.hidden)
        }
        .listStyle(.plain)
        .interactiveDismissDisabled()
        .navigationTitle("Icon")
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
        }
    }
}
