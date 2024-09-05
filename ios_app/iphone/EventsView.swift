import SwiftUI
import shared

private let menuTopPadding = 6.0
private let menuBottomPadding = menuTopPadding.goldenRatioUp().goldenRatioUp()

struct EventsView: View {

    @State private var vm = EventsVm()

    var body: some View {

        VMView(vm: vm) { state in

            VStack {

                if (state.isCalendarOrList) {
                    EventsCalendarView()
                } else {
                    EventsListView()
                }

                DividerBg()
                        .padding(.leading, H_PADDING)
                        .padding(.trailing, H_PADDING)

                HStack {

                    ModeButton(
                        text: "Calendar",
                        isActive: state.isCalendarOrList,
                        onClick: {
                            vm.setIsCalendarOrList(isCalendarOrList: true)
                        }
                    )
                            .padding(.leading, H_PADDING - halfDpCeil)

                    ModeButton(
                        text: "List",
                        isActive: !state.isCalendarOrList,
                        onClick: {
                            vm.setIsCalendarOrList(isCalendarOrList: false)
                        }
                    )
                            .padding(.leading, 8)

                    Spacer()
                }
                        .padding(.top, menuTopPadding)
                        .padding(.bottom, menuBottomPadding)
            }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}

//
// Mode Button

private let modeButtonShape = RoundedRectangle(cornerRadius: 8, style: .continuous)

private struct ModeButton: View {

    let text: String
    let isActive: Bool
    let onClick: () -> Void

    var body: some View {

        Button(
            action: {
                onClick()
            },
            label: {
                Text(text)
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(isActive ? c.white : c.text)
                        .padding(.horizontal, 7)
                        .padding(.top, 3)
                        .padding(.bottom, 3)

            }
        )
                .background(modeButtonShape.fill(isActive ? c.blue : c.transparent))
    }
}
