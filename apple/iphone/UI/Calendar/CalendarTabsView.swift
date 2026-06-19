import SwiftUI

struct CalendarTabsView: View {
    
    @State private var tab: CalendarTab = .calendar
    
    @Environment(\.dismiss) private var dismiss
    
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
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Close") {
                    dismiss()
                }
            }
        }
    }
}

private enum CalendarTab {
    case calendar
    case list
}
