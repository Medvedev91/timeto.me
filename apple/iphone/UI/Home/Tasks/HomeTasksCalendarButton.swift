import SwiftUI
import shared

struct HomeTasksCalendarButton: View {
    
    let color: Color
    let onClick: () -> Void
    
    var body: some View {
        HomeTasksIconButton(
            onClick: onClick,
            content: {
                Image(systemName: "calendar")
                    .font(.system(size: 20, weight: .regular))
                    .foregroundColor(color)
            },
        )
    }
}
