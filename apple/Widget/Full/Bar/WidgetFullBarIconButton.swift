import SwiftUI
import shared

struct WidgetFullBarIconButton<Content: View>: View {
    
    let taskFolderId: Int
    @ViewBuilder let content: () -> Content
    
    var body: some View {
        Link(destination: WidgetLink.TaskFolder(taskFolderId: taskFolderId).buildUrl()) {
            content()
        }
        .frame(
            width: widgetFullItemHeight,
            height: widgetFullItemHeight,
        )
    }
}
