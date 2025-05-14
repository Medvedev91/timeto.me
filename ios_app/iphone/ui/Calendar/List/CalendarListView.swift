import SwiftUI
import shared

struct CalendarListView: View {
    
    var body: some View {
        VmView({
            CalendarListVm()
        }) { _, state in
            CalendarListViewInner(
                state: state
            )
        }
    }
}

private struct CalendarListViewInner: View {
    
    let state: CalendarListVm.State
    
    var body: some View {
        
        ScrollView(.vertical, showsIndicators: false) {
            
            VStack {
                
                let eventsUi = state.eventsUi.reversed()
                ForEach(eventsUi, id: \.eventDb.id) { eventUi in
                    CalendarListItemView(
                        eventUi: eventUi,
                        withTopDivider: eventsUi.first != eventUi
                    )
                }
                
                Text(state.curTimeString)
                    .foregroundColor(.secondary)
                    .padding(.top, 16)
                    .padding(.bottom, 16)
            }
        }
        .defaultScrollAnchor(.bottom)
    }
}
