import SwiftUI

struct TextField__VMState: View {
    
    /// It's not VMState-like, but it's useful for UI logic.
    @FocusState.Binding private var isFocused: Bool
    
    /// TRICK
    /// Otherwise on init() with new text @State text would not updated.
    /// It is needed for view model if input hints exists.
    @State private var text: String
    private let stateText: String
    
    private let placeholder: String
    private let itemMinHeight: CGFloat
    private let onValueChanged: (String) -> Void
    
    init(
        text: String,
        placeholder: String,
        itemMinHeight: CGFloat,
        isFocused: FocusState<Bool>.Binding,
        onValueChanged: @escaping (String) -> Void
    ) {
        _isFocused = isFocused
        _text = State(initialValue: text)
        stateText = text
        self.placeholder = placeholder
        self.itemMinHeight = itemMinHeight
        self.onValueChanged = onValueChanged
    }
    
    var body: some View {
        
        ZStack(alignment: .trailing) {
            
            ZStack {
                TextField(
                    text: $text,
                    prompt: Text(placeholder),
                    axis: .vertical
                ) {
                    // todo what is it?
                }
                .padding(.vertical, 8)
            }
            .foregroundColor(c.text)
            ///
            .onChange(of: text) { newValue in
                onValueChanged(newValue)
            }
            .onChange(of: stateText) { newValue in
                text = newValue
            }
            ///
            .focused($isFocused)
            .textFieldStyle(.plain)
            .padding(.top, 1)
            .frame(minHeight: itemMinHeight)
            .padding(.leading, H_PADDING)
            .padding(.trailing, H_PADDING + 24) // for clear button
            
            TextFieldClearButtonView(
                text: $text,
                isFocused: $isFocused
            ) {
                isFocused = true
            }
            .padding(.trailing, 13)
        }
        .onTapGesture {
            isFocused = true
        }
    }
}
