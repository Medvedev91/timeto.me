import SwiftUI
import shared

private let bgColor = c.fg
private let hPadding = 8.0

struct EventsCalendarDayView: View {

    @State private var vm: EventsCalendarDayVM
    @EnvironmentObject private var nativeSheet: NativeSheet

    init(
        unixDay: Int
    ) {
        _vm = State(initialValue: EventsCalendarDayVM(unixDay: unixDay.toInt32()))
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            Divider(color: c.blue)

            HStack {

                Text(state.inNote)
                        .font(.system(size: 15))
                        .foregroundColor(.white)

                Spacer()

                Button(
                    action: {
                        nativeSheet.EventFormSheet__show(
                            editedEvent: nil,
                            defTime: state.formDefTime.toInt()
                        ) {
                        }
                    },
                    label: {
                        Text(state.newEventBtnText)
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(c.white)
                                .padding(.horizontal, 9)
                                .padding(.top, 4)
                                .padding(.bottom, 4)
                    }
                )
                        .background(roundedShape.fill(c.blue))
            }
                    .padding(.horizontal, hPadding)
                    .padding(.vertical, 12)
                    .background(bgColor)

            ForEachIndexed(state.eventsUi) { idx, eventUi in
                EventsListEventView(
                    eventUi: eventUi,
                    bgColor: bgColor,
                    paddingStart: hPadding,
                    paddingEnd: hPadding,
                    dividerColor: c.dividerFg,
                    withTopDivider: (idx > 0)
                )
            }
        }
    }
}
