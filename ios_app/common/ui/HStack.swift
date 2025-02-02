import SwiftUI

struct HStack<Content: View>: View {
    
    var alignment: VerticalAlignment = .center
    var spacing: CGFloat? = 0
    @ViewBuilder let content: () -> Content
    
    var body: some View {
        SwiftUI.HStack(
            alignment: alignment,
            spacing: spacing,
            content: content
        )
    }
}
