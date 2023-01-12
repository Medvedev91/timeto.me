import SwiftUI

struct MyListSection<Content: View>: View {

    var leadingPadding = 20.0
    var trailingPadding = 20.0
    @ViewBuilder var content: () -> Content

    var body: some View {
        VStack(spacing: 0) {
            content()
        }
                .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                .padding(.leading, leadingPadding)
                .padding(.trailing, trailingPadding)
                .padding(.top, 5)
    }
}
