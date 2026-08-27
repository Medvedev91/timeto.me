import WidgetKit
import SwiftUI

struct WidgetFull: Widget {
    
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "full_widget", provider: WidgetFullProvider()) { entry in
            if let widgetUi = entry.widgetUi {
                WidgetFullView(widgetUi: widgetUi)
                    .containerBackground(.black, for: .widget)
            } else {
                WidgetFullPlaceholderView()
                    .containerBackground(.black, for: .widget)
            }
        }
        .supportedFamilies([.systemLarge])
    }
}
