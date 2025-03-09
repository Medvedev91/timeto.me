import SwiftUI
import shared

struct ActivitiesScreen: View {
    
    var body: some View {
        VStack {
            ActivitiesView()
            BottomMenu()
        }
        .padding(.bottom, MainTabsView__HEIGHT)
    }
}

///

private struct BottomMenu: View {
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        HStack {
            
            MenuIconButton(
                text: "Summary",
                icon: "chart.pie"
            ) {
                navigation.sheet {
                    SummarySheet()
                }
            }
            .padding(.leading, 13)
            .padding(.trailing, 12)
            
            MenuIconButton(
                text: "History",
                icon: "list.bullet.rectangle"
            ) {
                navigation.sheet {
                    HistoryView()
                        .interactiveDismissDisabled()
                }
            }
            
            Spacer()
            
            Button(
                action: {
                    navigation.sheet {
                        EditActivitiesSheet()
                    }
                },
                label: {
                    Text("Edit")
                        .padding(.horizontal, ActivitiesView__timerHintHPadding)
                }
            )
            .padding(.trailing, ActivitiesView__listEngPadding)
        }
        .frame(height: ActivitiesView__listItemHeight)
        .background(.background)
    }
}

private struct MenuIconButton: View {
    
    let text: String
    let icon: String
    let onClick: () -> Void
    
    var body: some View {
        Button(
            action: {
                onClick()
            },
            label: {
                HStack {
                    Image(systemName: icon)
                        .font(.system(size: 18, weight: .light))
                        .padding(.trailing, 3 + onePx)
                    Text(text)
                }
            }
        )
    }
}
