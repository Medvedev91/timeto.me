import SwiftUI
import shared

struct SearchEmojiSheet: View {

    @State private var vm = SearchEmojiSheetVM()
    @Binding var isPresented: Bool
    let onSelect: (String) -> Void

    @FocusState private var isFocused: Bool
    @State private var inputText = ""

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            HStack(alignment: .center) {

                ZStack(alignment: .trailing) {

                    MyListView__ItemView(
                            isFirst: true,
                            isLast: true
                    ) {

                        HStack {

                            Image(systemName: "magnifyingglass")
                                    .foregroundColor(.secondary)
                                    .padding(.trailing, 10)

                            TextField(
                                    text: $inputText,
                                    prompt: Text(state.inputPlaceholder)
                            ) {
                                // todo what is it?
                            }
                                    .focused($isFocused)
                                    .textFieldStyle(.plain)
                                    .onChange(of: inputText) { newValue in
                                        vm.setInputValue(newValue: newValue)
                                    }
                        }
                                .padding(.leading, 12)
                                .padding(.vertical, 8)
                                .frame(maxWidth: .infinity)
                                .background(c.sheetFg)
                                .onTapGesture {
                                    isFocused = true
                                }
                    }
                            .padding(.top, 5)

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
                                    .padding(.top, 7)
                        }

                        TextFieldClearButtonView(
                                text: $inputText,
                                isFocused: $isFocused
                        ) {
                            isFocused = true
                        }
                                .padding(.top, 5)
                    }
                            .padding(.trailing, 25)
                }

                Button(
                        action: { isPresented.toggle() },
                        label: { Text("Cancel") }
                )
                        .padding(.top, 4)
                        .padding(.trailing, 24)
            }
                    .padding(.top, 10)

            MyListView__ItemView(
                    isFirst: true,
                    isLast: true
            ) {

                ScrollView {

                    MySpacerSize(height: 0)

                    let emojisRows: [GridItem] = Array(repeating: .init(.flexible(), spacing: 0), count: 8)
                    LazyVGrid(columns: emojisRows, spacing: 0) {

                        ForEach(state.selectedEmojis, id: \.self.emoji) { emoji in

                            Text(emoji.emoji)
                                    .font(.system(size: 33))
                                    .padding(.bottom, 4)
                                    // By button freezes on scroll // todo
                                    .onTapGesture {
                                        onSelect(emoji.emoji)
                                        isPresented = false
                                    }
                        }
                    }
                            .padding(.horizontal, 8)

                    MySpacerSize(height: 0)
                }
                        .background(c.sheetFg)
            }
                    .padding(.top, 10)
                    .padding(.bottom, 8)
        }
                .background(c.sheetBg)
                .onAppear {
                    for i in 0...5 {
                        myAsyncAfter(0.2 * i.toDouble()) {
                            isFocused = true
                        }
                    }
                }
    }
}
