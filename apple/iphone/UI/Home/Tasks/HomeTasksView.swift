import SwiftUI
import shared

struct HomeTasksView: View {
    
    let homeVm: HomeVm
    let homeState: HomeVm.State
    
    var body: some View {
        
        VStack {
            
            if !homeState.taskFolderUi.taskFolderDb.isToday {
                Text(homeState.taskFolderUi.taskFolderDb.name)
                    .font(.system(size: 30, weight: .bold))
                    .foregroundColor(.primary)
                    .padding(.leading, HomeScreen__hPadding)
                    .padding(.bottom, 8)
                    .textAlign(.leading)
            }
            
            ScrollView(showsIndicators: false) {
                VStack {
                    ForEach(homeState.homeTasksItemsUi.reversed(), id: \.id) { itemUi in
                        if let homeTaskUi = itemUi as? HomeTasksItemUi.HomeTaskUi {
                            HomeTaskView(
                                homeTaskUi: homeTaskUi,
                                homeState: homeState,
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
