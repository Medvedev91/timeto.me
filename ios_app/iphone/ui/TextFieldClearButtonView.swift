import SwiftUI

struct TextFieldClearButtonView: View {

    @Binding var text: String
    @FocusState.Binding var isFocused: Bool
    var onClick: () -> Void

    var body: some View {
        ZStack {
            if !text.isEmpty && isFocused {
                Button(
                    action: {
                        withAnimation(.easeInOut(duration: 0.2)) {
                            text = ""
                        }
                        onClick()
                    },
                    label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(c.tertiaryText)
                            .padding(.vertical, 4)
                    }
                )
                .transition(.opacity.animation(.easeInOut(duration: 0.1)))
            }
        }
    }
}
