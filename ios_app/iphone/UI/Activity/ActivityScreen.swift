import SwiftUI
import shared

struct ActivityScreen: View {
    
    @Binding var tab: MainTabEnum

    var body: some View {
        VStack {
            
            HistoryScreen()
            
            BottomMenu(
                openHomeTab: {
                    tab = .home
                }
            )
        }
        .padding(.bottom, MainTabsView__HEIGHT)
    }
}

///

private struct BottomMenu: View {
    
    let openHomeTab: () -> Void
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        HStack {
            
            MenuIconButton(
                text: "Summary",
            ) {
                navigation.sheet {
                    SummarySheet(
                        onClose: {
                            openHomeTab()
                        }
                    )
                }
            }
            .padding(.leading, 9)
            .padding(.trailing, 10)
            
            Spacer()
        }
    }
}

private struct MenuIconButton: View {
    
    let text: String
    let onClick: () -> Void
    
    var body: some View {
        Button(
            action: {
                onClick()
            },
            label: {
                Text(text)
                    .padding(.top, 4)
                    .padding(.bottom, 12)
            }
        )
    }
}
