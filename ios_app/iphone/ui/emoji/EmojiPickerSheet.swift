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

private let fgColor = Color(.secondarySystemBackground)
private let fgCornerRadius: CGFloat = 8
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
    @FocusState private var isFocused: Bool
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        
        VStack {
            
            HStack {
                
                ZStack(alignment: .trailing) {
                    
                    HStack {
                        
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(.secondary)
                            .padding(.trailing, 10)
                        
                        TextField(
                            state.searchPlaceholder,
                            text: $searchText
                        )
                        .focused($isFocused)
                        .onChange(of: searchText) { _, text in
                            vm.search(text: text)
                        }
                    }
                    .padding(.leading, 12)
                    .padding(.vertical, 8)
                    .background(fgColor)
                    .cornerRadius(fgCornerRadius)
                    .frame(maxWidth: .infinity)
                    .onTapGesture {
                        isFocused = true
                    }
                    
                    HStack {
                        
                        if isFocused {
                            Button(
                                action: {
                                    isFocused = false
                                },
                                label: {
                                    Image(systemName: "keyboard.chevron.compact.down")
                                        .foregroundColor(.secondary.opacity(0.8))
                                        .padding(.leading, 8)
                                        .padding(.trailing, 4)
                                        .padding(.vertical, 8)
                                }
                            )
                            .transition(.opacity.animation(.easeInOut(duration: 0.3)))
                            .padding(.top, 1)
                        }
                        
                        if !searchText.isEmpty {
                            ClearButton(
                                text: $searchText,
                                isFocused: $isFocused
                            )
                        }
                    }
                    .padding(.trailing, 4)
                }
                .padding(.leading, H_PADDING)
                
                Button("Cancel") {
                    dismiss()
                }
                .padding(.leading, 12)
                .padding(.trailing, H_PADDING)
            }
            .padding(.top, 8)
            
            ZStack {
                
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
                .background(fgColor)
                .cornerRadius(fgCornerRadius)
            }
            .padding(.horizontal, H_PADDING)
            .padding(.top, 10)
            .padding(.bottom, 10)
        }
    }
}

private struct ClearButton: View {
    
    @Binding var text: String
    @FocusState.Binding var isFocused: Bool
    
    var body: some View {
        Button(
            action: {
                withAnimation(.easeInOut(duration: 0.2)) {
                    text = ""
                }
                isFocused = true
            },
            label: {
                Image(systemName: "xmark.circle.fill")
                    .foregroundColor(Color(.tertiaryLabel))
            }
        )
        .padding(.leading, 8)
        .padding(.trailing, 8)
        .transition(.opacity.animation(.easeInOut(duration: 0.1)))
    }
}
