import SwiftUI

struct MyTextEditor: View {

    @Binding var text: String
    let placeholder: String
    var withAutoFocus = true

    @FocusState private var isFocused: Bool

    var body: some View {
        ZStack(alignment: .topLeading) {

            /// Transparent thanks to UITextView.appearance().backgroundColor = .clear
            TextEditor(text: $text)
                    .focused($isFocused)

            if text.isEmpty {
                Text(placeholder)
                        .padding(.leading, 5)
                        .padding(.top, 8)
                        .foregroundColor(.secondary)
            }
        }
                .onAppear {
                    // Less than 0.5 does not opened
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.55) {
                        if withAutoFocus {
                            isFocused = true
                        }
                    }
                    // Control
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                        if withAutoFocus {
                            isFocused = true
                        }
                    }
                }
    }
}
