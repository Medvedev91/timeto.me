import SwiftUI

struct ScrollViewWithVListener<Content>: View where Content: View {

    let showsIndicators: Bool
    @Binding var vScroll: Int
    @ViewBuilder let content: () -> Content

    ///

    private let spaceName = UUID().uuidString

    var body: some View {

        ScrollView(.vertical, showsIndicators: showsIndicators) {

            content()
                    .background(GeometryReader {
                        Color.clear.preference(
                                key: ViewOffsetKey.self,
                                value: -$0.frame(in: .named(spaceName)).origin.y
                        )
                    })
                    .onPreferenceChange(ViewOffsetKey.self) {
                        vScroll = Int($0)
                    }
        }
                .coordinateSpace(name: spaceName)
    }
}
