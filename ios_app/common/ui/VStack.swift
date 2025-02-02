import SwiftUI

struct VStack<Content: View>: View {
    
    var alignment: HorizontalAlignment = .center
    var spacing: CGFloat? = 0
    @ViewBuilder let content: () -> Content
    
    var body: some View {
        SwiftUI.VStack(
            alignment: alignment,
            spacing: spacing,
            content: content
        )
    }
}
