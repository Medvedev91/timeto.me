import SwiftUI

struct CalendarTabsView: View {
    
    @State private var tab: CalendarTab = .calendar
    
    var body: some View {
        
        VStack {
            
            Picker("", selection: $tab) {
                Text("Calendar")
                    .tag(CalendarTab.calendar)
                Text("List")
                    .tag(CalendarTab.list)
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, H_PADDING - 2)
            
            switch tab {
            case .calendar:
                CalendarView()
            case .list:
                CalendarListView()
                    .padding(.horizontal, H_PADDING)
            }
        }
    }
}

private enum CalendarTab {
    case calendar
    case list
}
