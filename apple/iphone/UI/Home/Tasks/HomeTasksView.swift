import SwiftUI
import shared

struct HomeTasksView: View {
    
    let homeModeTaskFolder: HomeMode.TaskFolder
    
    var body: some View {
        
        VStack {
            
            if !homeModeTaskFolder.taskFolderDb.isToday {
                Text(homeModeTaskFolder.taskFolderDb.name)
                    .font(.system(size: 30, weight: .bold))
                    .foregroundColor(.primary)
                    .padding(.leading, HomeScreen__hPadding)
                    .padding(.bottom, 8)
                    .textAlign(.leading)
            }
            
            ScrollView(showsIndicators: false) {
                VStack {
                    ForEach(homeModeTaskFolder.homeTasksItemsUi.reversed(), id: \.id) { itemUi in
                        if let homeTaskUi = itemUi as? HomeTasksItemUi.HomeTaskUi {
                            HomeTaskView(
                                homeTaskUi: homeTaskUi,
                                homeModeTaskFolder: homeModeTaskFolder,
                            )
                        } else if let homeTomorrowItemUi = itemUi as? HomeTasksItemUi.HomeTomorrowItemUi {
                            HomeTasksTomorrowView(
                                tomorrowUi: homeTomorrowItemUi,
                            )
                        }
                    }
                }
            }
            .defaultScrollAnchor(.bottom)
        }
    }
}
