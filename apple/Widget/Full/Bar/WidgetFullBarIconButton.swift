import SwiftUI
import shared

struct WidgetFullBarIconButton<Content: View>: View {
    
    let widgetLink: WidgetLink
    @ViewBuilder let content: () -> Content
    
    var body: some View {
        Link(destination: widgetLink.buildUrl()) {
            content()
        }
        .frame(
            width: widgetFullItemHeight,
            height: widgetFullItemHeight,
        )
    }
}
