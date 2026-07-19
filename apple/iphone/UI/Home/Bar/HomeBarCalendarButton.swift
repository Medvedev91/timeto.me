import SwiftUI
import shared

struct HomeBarCalendarButton: View {
    
    let color: Color
    let onClick: () -> Void
    
    var body: some View {
        HomeBarIconButton(
            onClick: onClick,
            content: {
                Image(systemName: "calendar")
                    .font(.system(size: 20, weight: .regular))
                    .foregroundColor(color)
            },
        )
    }
}
