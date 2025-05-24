import SwiftUI
import Combine
import shared

struct WatchTabsView: View {
    
    static var lastInstance: WatchTabsView? = nil
    
    static let TAB_ID_TIMER = 1
    static let TAB_ID_TASKS = 2
    
    @State var tabSelection = TAB_ID_TIMER
    
    var body: some View {
        
        let _ = WatchTabsView.lastInstance = self
        
        TabView(selection: $tabSelection) {
            
            WatchTabTimerView()
                .tag(WatchTabsView.TAB_ID_TIMER)
            
            WatchTabTasksView()
                .tag(WatchTabsView.TAB_ID_TASKS)
        }
    }
}
