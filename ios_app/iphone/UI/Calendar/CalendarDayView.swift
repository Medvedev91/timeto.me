import SwiftUI
import shared

struct CalendarDayView: View {
    
    let unixDay: Int
    
    var body: some View {
        VmView({
            CalendarDayVm(
                unixDay: unixDay.toInt32()
            )
        }) { _, state in
            CalendarDayViewInner(
                state: state
            )
        }
        .id("CalendarDayView_\(unixDay)")
    }
}

private struct CalendarDayViewInner: View {

    let state: CalendarDayVm.State
    
    ///

    @Environment(Navigation.self) private var navigation
    
    var body: some View {

        VStack {

            Divider()
                .overlay(.blue)

            HStack {

                Text(state.inNote)
                        .font(.system(size: 15))
                        .foregroundColor(.white)

                Spacer()

                Button(
                    action: {
                        navigation.fullScreen(withAnimation: false) {
                            EventFormFullScreen(
                                initEventDb: nil,
                                initText: nil,
                                initTime: state.initTime.toInt(),
                                onDone: {}
                            )
                        }
                    },
                    label: {
                        Text(state.newEventText)
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(.white)
                                .padding(.horizontal, 9)
                                .padding(.top, 4)
                                .padding(.bottom, 4)
                    }
                )
                        .background(roundedShape.fill(.blue))
            }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 12)

            
            let itemsUi = state.itemsUi
            ForEach(itemsUi, id: \.self) { itemUi in
                let isFirst = itemsUi.first == itemUi
                if let eventItemUi = itemUi as? CalendarDayVm.ItemUiEventUi {
                    CalendarListItemView(
                        eventUi: eventItemUi.calendarListEventUi,
                        withTopDivider: !isFirst,
                    )
                    .padding(.horizontal, 8)
                }
                else if let repeatingItemUi = itemUi as? CalendarDayVm.ItemUiRepeatingUi {
                    TasksTabRepeatingsItemView(
                        repeatingUi: repeatingItemUi.repeatingsListRepeatingUi,
                        withTopDivider: !isFirst,
                    )
                    .padding(.horizontal, 8)
                }
                else {
                    fatalError()
                }
            }
        }
        .background(Color(.secondarySystemBackground))
    }
}
