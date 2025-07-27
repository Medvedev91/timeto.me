import SwiftUI

@main
struct WidgetBundle: SwiftUI.WidgetBundle {
    
    @WidgetBundleBuilder
    var body: some SwiftUI.Widget {
        Widget()
        // WidgetStatic()
    }
}
