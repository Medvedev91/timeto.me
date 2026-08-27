import SwiftUI
import shared

struct WidgetFullButtonsView: View {
    
    let widgetUi: WidgetUi
    
    var body: some View {
        WidgetFullButtonsViewLocal(widgetUi: widgetUi)
            .padding(.horizontal, widgetFullHPadding)
            .frame(height: CGFloat(widgetUi.height))
    }
}

private struct WidgetFullButtonsViewLocal: View {
    
    let widgetUi: WidgetUi

    var body: some View {
        ZStack(alignment: .topLeading) {
            Color.clear
            
            ForEach(widgetUi.homeButtonsUi, id: \.id) { buttonUi in
                ZStack {
                    if let activity = buttonUi.type as? HomeButtonType.Activity {
                        WidgetFullButtonsActivityView(activity: activity)
                    } else {
                        fatalError()
                    }
                }
                .frame(width: CGFloat(buttonUi.width), height: widgetFullItemHeight)
                .offset(x: CGFloat(buttonUi.offsetX), y: CGFloat(buttonUi.offsetY))
            }
        }
        .fillMaxSize()
    }
}
