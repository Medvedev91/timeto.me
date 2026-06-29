import SwiftUI
import shared

struct SymbolPickerSheet: View {
    
    let onPick: (Symbol) -> Void

    var body: some View {
        VmView({ SymbolPickerVm() }) { vm, _ in
            let state = vm.state.value as! SymbolPickerVm.State
            SymbolPickerSheetInner(
                state: state,
                onPick: onPick,
            )
        }
    }
}

private struct SymbolPickerSheetInner: View {
    
    let state: SymbolPickerVm.State
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
            
            ForEach(state.symbolChunks, id: \.self) { row in
                HStack {
                    Spacer()
                    ForEach(row, id: \.self.raw) { symbol in
                        Button(
                            action: {
                                onPick(symbol)
                                dismiss()
                            },
                            label: {
                                SymbolView(
                                    symbol: symbol,
                                    color: .white,
                                    letterSize: 23, // No matter
                                    iconSize: 18,
                                    emojiSize: 12, // No matter
                                )
                            }
                        )
                        .buttonStyle(.plain)
                        Spacer()
                    }
                }
                .listRowSeparator(.hidden)
            }
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
