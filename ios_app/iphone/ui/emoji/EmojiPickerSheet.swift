import SwiftUI
import shared

struct EmojiPickerSheet: View {
    
    let onPick: (String) -> Void
    
    var body: some View {
        VmView({
            EmojiPickerVm()
        }) { vm, state in
            EmojiPickerSheetInner(
                vm: vm,
                state: state,
                onPick: onPick
            )
        }
    }
}

///

private let emojisRows: [GridItem] = Array(
    repeating: .init(.flexible(), spacing: 0),
    count: 8
)

private struct EmojiPickerSheetInner: View {
    
    let vm: EmojiPickerVm
    let state: EmojiPickerVm.State
    
    let onPick: (String) -> Void
    
    ///
    
    @State private var searchText = ""
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        
        ScrollView {
            
            LazyVGrid(columns: emojisRows, spacing: 0) {
                
                ForEach(state.emojis, id: \.self.emoji) { emoji in
                    
                    // Button() freezes on scroll // todo
                    Text(emoji.emoji)
                        .font(.system(size: 33))
                        .padding(.bottom, 4)
                        .onTapGesture {
                            onPick(emoji.emoji)
                            dismiss()
                        }
                }
            }
            .padding(.horizontal, 8)
        }
        .contentMargins(.vertical, 10)
        .toolbar {
            
            ToolbarItem(placement: .principal) {
                
                HStack {
                    
                    SearchBar(
                        text: $searchText,
                        placeholder: "Search",
                        returnKeyType: .done,
                        onDone: {
                            hideKeyboard()
                        }
                    )
                    .onChange(of: searchText) { _, text in
                        vm.search(text: text)
                    }
                    
                    Button("Cancel") {
                        dismiss()
                    }
                    .buttonStyle(.plain)
                    .foregroundColor(.blue)
                    .padding(.leading, 14)
                }
            }
        }
        .toolbarTitleDisplayMode(.inline)
    }
}
