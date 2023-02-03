import SwiftUI

struct TextField__VMState: View {

    @FocusState private var focusedField: Bool

    /// TRICK
    /// Otherwise on init() with new text @State text would not updated.
    /// It is needed for view model if input hints exists.
    @State private var text: String
    private let stateText: String

    private let placeholder: String
    private let isAutofocus: Bool
    private let onValueChanged: (String) -> Void

    init(
            text: String,
            placeholder: String,
            isAutofocus: Bool,
            onValueChanged: @escaping (String) -> Void
    ) {
        _text = State(initialValue: text)
        stateText = text
        self.placeholder = placeholder
        self.isAutofocus = isAutofocus
        self.onValueChanged = onValueChanged
    }

    var body: some View {

        ZStack(alignment: .trailing) {

            ZStack {
                if #available(iOS 16.0, *) {
                    TextField(
                            text: $text,
                            prompt: Text(placeholder),
                            axis: .vertical
                    ) {
                        // todo what is it?
                    }
                            .padding(.vertical, 8)
                } else {
                    // One line ;(
                    TextField(text: $text, prompt: Text(placeholder)) {}
                }
            }
                    ///
                    .onChange(of: text) { newValue in
                        onValueChanged(newValue)
                    }
                    .onChange(of: stateText) { newValue in
                        text = newValue
                    }
                    ///
                    .focused($focusedField)
                    .textFieldStyle(.plain)
                    .frame(minHeight: MyListView.ITEM_MIN_HEIGHT)
                    .padding(.leading, MyListView.PADDING_SECTION_ITEM_INNER_HORIZONTAL)
                    .padding(.trailing, MyListView.PADDING_SECTION_ITEM_INNER_HORIZONTAL + 16) // for clear button

            TextFieldClearButtonView(
                    text: $text,
                    trailingPadding: 8
            ) {
                focusedField = true
            }
        }
                .onTapGesture {
                    focusedField = true
                }
                .onAppear {
                    if isAutofocus {
                        for i in 0...10 {
                            myAsyncAfter(0.1 * i.toDouble()) {
                                focusedField = true
                            }
                        }
                    }
                }
    }
}
