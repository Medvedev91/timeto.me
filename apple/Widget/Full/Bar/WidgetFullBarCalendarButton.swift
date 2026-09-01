import SwiftUI
import shared

struct WidgetFullBarCalendarButton: View {
    
    let color: Color
    
    var body: some View {
        WidgetFullBarIconButton(
            widgetLink: WidgetLink.Calendar,
            content: {
                Image(systemName: "calendar")
                    .font(.system(size: 20, weight: .regular))
                    .foregroundColor(color)
            },
        )
    }
}
