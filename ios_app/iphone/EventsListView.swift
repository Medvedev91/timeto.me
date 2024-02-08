import SwiftUI
import shared

struct EventsListView: View {

    @State private var vm = EventsListVM()

    /// Avoiding animation on start, but it is needed for editing
    @State private var useAnimation = false

    @EnvironmentObject private var nativeSheet: NativeSheet

    private let LIST_BOTTOM_ITEM_ID = "bottom_id"

    var body: some View {

        VMView(vm: vm) { state in

            GeometryReader { geometry in

                ScrollViewReader { scrollProxy in

                    ScrollView(.vertical, showsIndicators: false) {

                        VStack {

                            Spacer()

                            VStack {
                                let uiEvents = state.uiEvents.reversed()
                                ForEach(uiEvents, id: \.event.id) { uiEvent in
                                    let isFirst = uiEvents.first == uiEvent
                                    ZStack(alignment: .top) {
                                        EventsListEventView(
                                            eventUi: uiEvent,
                                            bgColor: c.bg,
                                            paddingStart: H_PADDING,
                                            paddingEnd: 0,
                                            withTopDivider: !isFirst
                                        )
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

                            EventTemplatesView(
                                spaceAround: H_PADDING,
                                paddingTop: 20
                            )

                            ZStack(alignment: .trailing) {

                                Button(
                                    action: {
                                        nativeSheet.EventFormSheet__show(editedEvent: nil) {
                                            scrollDown(scrollProxy: scrollProxy, toAnimate: true)
                                        }
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
                                    .overlay(squircleShape.stroke(c.dividerBg, lineWidth: onePx))
                                    .padding(.top, 19)
                                    .padding(.bottom, 20)
                                    .padding(.leading, H_PADDING)

                            HStack {
                            }
                                    .id(LIST_BOTTOM_ITEM_ID)
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
