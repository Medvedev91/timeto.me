import SwiftUI
import shared

struct TmrwPeekView: View {

    @State private var vm = TmrwPeekVM()

    private let LIST_BOTTOM_ITEM_ID = "bottom_id"

    var body: some View {

        VMView(vm: vm) { state in

            GeometryReader { geometry in

                ScrollViewReader { scrollProxy in

                    ScrollView(.vertical, showsIndicators: false) {

                        VStack(spacing: 0) {

                            Spacer()

                            let tasksUIReversed = state.tasksUI.reversed()
                            VStack(spacing: 0) {
                                ForEach(tasksUIReversed, id: \.task.id) { taskUI in
                                    let isFirst = tasksUIReversed.first == taskUI
                                    let isLast = tasksUIReversed.last == taskUI
                                    MyListView__ItemView(
                                            isFirst: isFirst,
                                            isLast: isLast,
                                            withTopDivider: !isFirst,
                                            outerPaddingStart: 0,
                                            outerPaddingEnd: 0
                                    ) {
                                        TmrwPeekView__TaskRowView(taskUI: taskUI)
                                    }
                                }
                            }
                                    .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))

                            HStack {
                                Spacer()
                                Text(state.curTimeString)
                                        .font(.system(size: 14, weight: .light))

                                Spacer()
                            }
                                    .padding(.leading, MyListView.PADDING_OUTER_HORIZONTAL)
                                    .padding(.vertical, 24)

                            HStack {
                            }
                                    .id(LIST_BOTTOM_ITEM_ID)
                        }
                                .frame(minHeight: geometry.size.height)
                    }
                            ///
                            .onAppear {
                                scrollDown(scrollProxy: scrollProxy, toAnimate: false)
                            }
                }
            }
                    .padding(.leading, 16)
                    .padding(.trailing, 20)
        }
    }

    private func scrollDown(
            scrollProxy: ScrollViewProxy,
            toAnimate: Bool
    ) {
        if (toAnimate) {
            withAnimation {
                scrollProxy.scrollTo(LIST_BOTTOM_ITEM_ID, anchor: .bottom)
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                withAnimation {
                    scrollProxy.scrollTo(LIST_BOTTOM_ITEM_ID, anchor: .bottom)
                }
            }
        } else {
            scrollProxy.scrollTo(LIST_BOTTOM_ITEM_ID, anchor: .bottom)
            for i in 0...4 {
                let delay = 0.1 * i.toDouble()
                myAsyncAfter(delay) {
                    scrollProxy.scrollTo(LIST_BOTTOM_ITEM_ID, anchor: .bottom)
                }
            }
        }
    }
}

private struct TmrwPeekView__TaskRowView: View {

    let taskUI: TmrwPeekVM.TaskUI

    var body: some View {

        let paddingStart = 16.0

        VStack(spacing: 0) {

            let vPadding = 8.0

            if let timeUI = taskUI.textFeatures.timeUI {
                HStack {
                    Text(timeUI.daytimeText)
                            .padding(.leading, paddingStart)
                            .padding(.top, 1)
                            .padding(.bottom, vPadding)
                            .font(.system(size: 14, weight: .light))
                            .foregroundColor(timeUI.color.toColor())
                            .lineLimit(1)
                    Spacer()
                }
            }

            HStack {
                /// It can be multiline
                Text(taskUI.listText)
                        .padding(.leading, paddingStart)
                        .padding(.trailing, 16)
                        .lineSpacing(4)
                        .multilineTextAlignment(.leading)
                        .myMultilineText()

                Spacer(minLength: 0)
            }

            TriggersView__List(triggers: taskUI.textFeatures.triggers)
                    .padding(.top, taskUI.textFeatures.triggers.isEmpty ? 0 : vPadding)
        }
                .padding(.vertical, 10)
    }
}
