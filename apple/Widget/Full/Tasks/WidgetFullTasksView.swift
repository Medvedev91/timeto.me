import SwiftUI
import shared

struct WidgetFullTasksView: View {
    
    let homeModeTaskFolder: HomeMode.TaskFolder
    
    var body: some View {
        LazyVStack(spacing: 0) {
            ForEach(homeModeTaskFolder.homeTasksItemsUi.reversed(), id: \.id) { itemUi in
                if let homeTaskUi = itemUi as? HomeTasksItemUi.HomeTaskUi {
                    WidgetFullTaskView(
                        homeTaskUi: homeTaskUi,
                    )
                } else if let homeTomorrowItemUi = itemUi as? HomeTasksItemUi.HomeTomorrowItemUi {
                    // todo UI for Widget
                }
            }
        }
    }
}
