import SwiftUI
import shared

struct EventsListEventView: View {

    let eventUi: EventsListVM.EventUi
    let bgColor: Color
    let paddingStart: CGFloat
    let paddingEnd: CGFloat
    let dividerColor: Color
    let withTopDivider: Bool

    @EnvironmentObject private var fs: Fs

    var body: some View {

        ZStack(alignment: .top) {

            MyListSwipeToActionItem(
                deletionHint: eventUi.event.text,
                deletionConfirmationNote: eventUi.deletionNote,
                onEdit: {
                    fs.EventFormSheet__show(editedEvent: eventUi.event) {
                    }
                },
                onDelete: {
                    withAnimation {
                        eventUi.delete()
                    }
                }
            ) {

                AnyView(safeView)
                        .padding(.leading, paddingStart)
                        .padding(.trailing, paddingEnd)
                        // todo remove after removing MyListSwipeToActionItem()
                        .background(bgColor)
            }

            if withTopDivider {
                Divider(color: dividerColor)
                        .padding(.leading, paddingStart)
                        .padding(.trailing, paddingEnd)
            }
        }
    }

    private var safeView: some View {

        VStack {

            HStack {

                Text(eventUi.dateString)
                        .font(.system(size: 14, weight: .light))
                        .foregroundColor(.secondary)

                Spacer()

                Text(eventUi.dayLeftString)
                        .font(.system(size: 14, weight: .light))
                        .foregroundColor(.secondary)
            }

            HStack {

                Text(eventUi.listText)
                        .lineSpacing(4)
                        .multilineTextAlignment(.leading)
                        .myMultilineText()

                Spacer()

                TriggersListIconsView(triggers: eventUi.textFeatures.triggers, fontSize: 15)
            }
                    .padding(.top, 4)
        }
                .padding(.top, 10)
                .padding(.bottom, 10)
                .foregroundColor(.primary)
                .id("\(eventUi.event.id) \(eventUi.event.text)") /// #TruncationDynamic

    }
}
