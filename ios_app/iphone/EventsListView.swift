import SwiftUI
import shared

struct EventsListView: View {

    @State private var vm = EventsListVm()

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
                                            dividerColor: c.dividerBg,
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
