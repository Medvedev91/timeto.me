import SwiftUI

struct TextFieldClearButtonView: View {

    @Binding var text: String
    var leadingPadding = 8.0
    var trailingPadding: CGFloat
    var onClick: () -> Void

    var body: some View {
        ZStack {
            if !text.isEmpty {
                Button(
                        action: {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                text = ""
                            }
                            onClick()
                        },
                        label: {
                            Image(systemName: "xmark.circle.fill")
                                    .foregroundColor(.secondary.opacity(0.5))
                                    .padding(.leading, leadingPadding)
                                    .padding(.trailing, trailingPadding)
                                    .padding(.vertical, 4)
                        }
                )
                        .transition(.opacity.animation(.easeInOut(duration: 0.1)))
            }
        }
    }
}
