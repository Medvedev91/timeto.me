import SwiftUI
import shared

struct CalendarListView: View {
    
    var body: some View {
        VmView({
            CalendarListVm()
        }) { vm, state in
            let state = vm.state.value as! CalendarListVm.State
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
                
                ZStack {}
                    .frame(height: 16)
                
                let eventsUi = state.eventsUi.reversed()
                ForEach(eventsUi, id: \.eventDb.id) { eventUi in
                    CalendarListItemView(
                        eventUi: eventUi,
                        withTopDivider: eventsUi.first != eventUi
                    )
                }
                
                ZStack {}
                    .frame(height: 16)
            }
        }
        .defaultScrollAnchor(.bottom)
    }
}
