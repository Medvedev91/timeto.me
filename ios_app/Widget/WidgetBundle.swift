import SwiftUI

@main
struct WidgetBundle: SwiftUI.WidgetBundle {
    
    @WidgetBundleBuilder
    var body: some Widget {
        WidgetLive()
        // WidgetStatic()
    }
}
