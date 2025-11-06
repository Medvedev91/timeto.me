import SwiftUI
import shared

struct ActivityScreen: View {
    
    @Binding var tab: MainTabEnum

    var body: some View {
        ZStack(alignment: .bottom) {
            
            HistoryScreen(tab: $tab)
            
            MenuView()
        }
        .padding(.bottom, MainTabsView__HEIGHT)
    }
}

///

private struct MenuView: View {
    
    var body: some View {
        HStack {
            MenuButton(text: "List", isSelected: true)
            MenuSeparator()
            MenuButton(text: "Today", isSelected: false)
            MenuButton(text: "Yesterday", isSelected: false)
            MenuButton(text: "7d", isSelected: false)
            MenuButton(text: "30d", isSelected: false)
            MenuSeparator()
            MenuButton(text: "16 Dec", isSelected: false)
        }
        .padding(.horizontal, 4)
        .padding(.vertical, 8)
        .background(squircleShape.fill(.black.opacity(0.8)))
        .padding(.bottom, 12)
    }
}

private struct MenuButton: View {
    
    let text: String
    let isSelected: Bool
    
    var body: some View {
        Button(
            action: {
            },
            label: {
                Text(text)
                    .lineLimit(1)
                    .padding(.horizontal, 6)
                    .foregroundColor(isSelected ? .primary : .secondary)
                    .font(.system(size: 15, weight: isSelected ? .semibold : .regular))
                    .minimumScaleFactor(0.2)
            }
        )
    }
}

private struct MenuSeparator: View {
    
    var body: some View {
        Divider()
            .background(.white.opacity(0.9))
            .frame(height: 16)
            .padding(.horizontal, 6)
    }
}
