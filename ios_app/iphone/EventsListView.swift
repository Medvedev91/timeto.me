import SwiftUI
import shared

struct EventsListView: View {

    @State private var vm = EventsListVM()

    ///
    /// WARNING Do use showAddCalendar()
    @State private var addCalendarInitHistoryItem: EventsHistory.Item? = nil
    @State private var isAddCalendarPresented = false
    @State private var wtfGuys = false
    //////

    /// Avoiding animation on start, but it is needed for editing
    @State private var useAnimation = false

    private let LIST_BOTTOM_ITEM_ID = "bottom_id"

    var body: some View {

        VMView(vm: vm) { state in

            GeometryReader { geometry in

                ScrollViewReader { scrollProxy in

                    ScrollView(.vertical, showsIndicators: false) {

                        VStack(spacing: 0) {

                            Spacer()

                            VStack(spacing: 0) {
                                let uiEvents = state.uiEvents.reversed()
                                ForEach(uiEvents, id: \.event.id) { uiEvent in
                                    let isFirst = uiEvents.first == uiEvent
                                    ZStack(alignment: .top) {
                                        EventItemView(uiEvent: uiEvent)
                                        if !isFirst {
                                            DividerBg(xOffset: TAB_TASKS_PADDING_HALF_H)
                                        }
                                    }
                                }
                            }
                                    .padding(.bottom, 20)

                            HStack {
                                Spacer()
                                Text(state.curTimeString)
                                        .font(.system(size: 14, weight: .light))

                                Spacer()
                            }
                                    .padding(.leading, MyListView.PADDING_OUTER_HORIZONTAL)

                            EventsHistoryView(
                                    spaceAround: MyListView.PADDING_OUTER_HORIZONTAL + 1,
                                    paddingTop: 20
                            ) { historyItem in
                                showAddCalendar(
                                        initHistoryItem: historyItem
                                )
                            }

                            ZStack(alignment: .trailing) {

                                Button(
                                        action: {
                                            showAddCalendar()
                                        },
                                        label: {
                                            Text("Event")
                                                    .foregroundColor(.primary)
                                                    .multilineTextAlignment(.leading)
                                                    .opacity(0.4)
                                                    .frame(maxWidth: .infinity, alignment: .leading)
                                                    .padding(.leading, 18)
                                                    .padding(.trailing, 6)
                                                    .padding(.vertical, 8)
                                        }
                                )

                                Text("DATE")
                                        .font(.system(size: 14, weight: .bold))
                                        .frame(width: 68, height: 34)
                                        .foregroundColor(.white)
                                        .background(RoundedRectangle(cornerRadius: 8, style: .continuous).fill(.blue.opacity(0.6)))
                                        .padding(.trailing, 5)
                            }
                                    .padding(.top, 5)
                                    .padding(.bottom, 5)
                                    .background(RoundedRectangle(cornerRadius: 10, style: .continuous).fill(Color(.mySecondaryBackground)))
                                    .padding(.top, 19)
                                    .padding(.bottom, 20)
                                    .padding(.leading, MyListView.PADDING_OUTER_HORIZONTAL)

                            HStack {
                            }
                                    .id(LIST_BOTTOM_ITEM_ID)
                        }
                                .padding(.horizontal, TAB_TASKS_PADDING_HALF_H)
                                .sheetEnv(
                                        isPresented: $isAddCalendarPresented
                                ) {
                                    ZStack {
                                        if wtfGuys {
                                            EventFormSheet(
                                                    isPresented: $isAddCalendarPresented,
                                                    editedEvent: nil,
                                                    defText: addCalendarInitHistoryItem?.raw_title ?? "",
                                                    defDate: Date().startOfDay().inSeconds(addCalendarInitHistoryItem?.daytime.toInt() ?? 0)
                                            ) {
                                                scrollDown(scrollProxy: scrollProxy, toAnimate: true)
                                            }
                                        }
                                    }
                                            .onAppear { wtfGuys = true }
                                            .onDisappear { wtfGuys = false }
                                }

                                .frame(minHeight: geometry.size.height)
                    }
                            .animation(useAnimation ? Animation.easeOut(duration: 0.25) : nil)
                            .onAppear {
                                scrollDown(scrollProxy: scrollProxy, toAnimate: false)
                            }
                }
            }
        }
                ///
                .onAppear {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                        useAnimation = true
                    }
                }
                .onDisappear {
                    useAnimation = false
                }
                ///
                .padding(.trailing, 20)
    }

    private func showAddCalendar(
            initHistoryItem: EventsHistory.Item? = nil
    ) {
        // Ordering is important
        addCalendarInitHistoryItem = initHistoryItem
        isAddCalendarPresented = true
        hideKeyboard()
    }

    private func scrollDown(
            scrollProxy: ScrollViewProxy,
            toAnimate: Bool
    ) {
        if (toAnimate) {
            withAnimation {
                scrollProxy.scrollTo(LIST_BOTTOM_ITEM_ID, anchor: .bottom)
            }
            /// Scroll down. Without it, it does not complete for ~5px. WTF?!
            /// It does not work less than 0.03.
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                withAnimation {
                    scrollProxy.scrollTo(LIST_BOTTOM_ITEM_ID, anchor: .bottom)
                }
            }
        } else {
            scrollProxy.scrollTo(LIST_BOTTOM_ITEM_ID, anchor: .bottom)
        }
    }
}


private struct EventItemView: View {

    @State private var isEditEventPresented = false

    let uiEvent: EventsListVM.UiEvent

    var body: some View {
        MyListSwipeToActionItem(
                deletionHint: uiEvent.event.text,
                deletionConfirmationNote: uiEvent.deletionNote,
                onEdit: {
                    isEditEventPresented = true
                },
                onDelete: {
                    withAnimation {
                        uiEvent.delete()
                    }
                }
        ) {
            AnyView(safeView)
                    // todo remove after removing MyListSwipeToActionItem()
                    .background(Color(.bg))
        }
    }

    private var safeView: some View {

        VStack(spacing: 0) {

            HStack {
                Text(uiEvent.dateString)
                        .font(.system(size: 14, weight: .light))
                        .foregroundColor(.secondary)
                Spacer()
                Text(uiEvent.dayLeftString)
                        .font(.system(size: 14, weight: .light))
                        .foregroundColor(.secondary)
            }
                    .padding(.leading, TAB_TASKS_PADDING_HALF_H)
                    .padding(.trailing, TAB_TASKS_PADDING_HALF_H)

            HStack {
                Text(uiEvent.listText)
                        .myMultilineText()
                Spacer()
            }
                    .padding(.top, 4)
                    .padding(.leading, TAB_TASKS_PADDING_HALF_H)
                    .padding(.trailing, TAB_TASKS_PADDING_HALF_H)

            TextFeaturesTriggersView(
                    textFeatures: uiEvent.textFeatures,
                    contentPaddingStart: TAB_TASKS_PADDING_HALF_H - 1.0
            )
                    .padding(.top, uiEvent.textFeatures.triggers.isEmpty ? 0 : 8)
        }
                .padding(.top, 10)
                .padding(.bottom, 10)
                .foregroundColor(.primary)
                .sheetEnv(
                        isPresented: $isEditEventPresented,
                        content: {
                            EventFormSheet(
                                    isPresented: $isEditEventPresented,
                                    editedEvent: uiEvent.event
                            )
                        }
                )
                .id("\(uiEvent.event.id) \(uiEvent.event.text)") /// #TruncationDynamic
    }
}
