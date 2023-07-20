import SwiftUI
import shared

struct RepeatingsListView: View {

    @State private var vm = RepeatingsListVM()

    @Environment(\.defaultMinListRowHeight) private var minListRowHeight

    @State private var isAddRepeatingPresented = false

    private let LIST_BOTTOM_ITEM_ID = "bottom_id"

    var body: some View {

        VMView(vm: vm) { state in

            GeometryReader { geometry in

                ScrollViewReader { scrollProxy in

                    ScrollView(.vertical, showsIndicators: false) {

                        VStack {

                            Spacer()

                            VStack {
                                let repeatingsUI = state.repeatingsUI.reversed()
                                ForEach(repeatingsUI, id: \.repeating.id) { repeatingUI in
                                    let isFirst = repeatingsUI.first == repeatingUI
                                    ZStack(alignment: .top) {
                                        RepeatingsView__ItemView(repeatingUI: repeatingUI)
                                        if !isFirst {
                                            DividerBg()
                                                    .padding(.leading, TAB_TASKS_PADDING_HALF_H)
                                        }
                                    }
                                }
                            }
                                    .padding(.bottom, 20)

                            Button(
                                    action: {
                                        withAnimation {
                                            isAddRepeatingPresented = true
                                        }
                                    },
                                    label: {
                                        Text("New Repeating Task")
                                                .font(.system(size: 15, weight: .bold))
                                                .frame(maxWidth: .infinity, maxHeight: .infinity)
                                                .foregroundColor(.white)
                                                .background(squircleShape.fill(.blue))
                                    }
                            )
                                    .frame(height: minListRowHeight)
                                    .sheetEnv(
                                            isPresented: $isAddRepeatingPresented
                                    ) {
                                        RepeatingsFormSheet(
                                                isPresented: $isAddRepeatingPresented,
                                                editedRepeating: nil
                                        ) {
                                            scrollDown(scrollProxy: scrollProxy, toAnimate: false)
                                        }
                                    }
                                    .padding(.bottom, 20)
                                    .padding(.horizontal, TAB_TASKS_PADDING_HALF_H - 4.0)

                            HStack {
                            }
                                    .id(LIST_BOTTOM_ITEM_ID)
                        }
                                .frame(minHeight: geometry.size.height)
                    }
                            .onAppear {
                                scrollDown(scrollProxy: scrollProxy, toAnimate: false)
                            }
                }
            }
                    .padding(.trailing, TAB_TASKS_PADDING_HALF_H)
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

struct RepeatingsView__ItemView: View {

    let repeatingUI: RepeatingsListVM.RepeatingUI

    @State private var isEditSheetPresented = false

    var body: some View {
        MyListSwipeToActionItem(
                deletionHint: repeatingUI.listText,
                deletionConfirmationNote: repeatingUI.deletionNote,
                onEdit: {
                    isEditSheetPresented = true
                },
                onDelete: {
                    withAnimation {
                        // WARNING WTF! // todo is it actual after migration to KMM?
                        // Without async the getPeriod() calls with NPE on typeId
                        myAsyncAfter(0.1) {
                            repeatingUI.delete()
                        }
                    }
                }
        ) {
            AnyView(safeView)
                    // todo remove after removing MyListSwipeToActionItem()
                    .background(Color(.bg))
        }
    }

    private var safeView: some View {

        VStack {

            HStack {
                Text(repeatingUI.dayLeftString)
                        .font(.system(size: 14, weight: .light))
                        .foregroundColor(.secondary)

                Spacer()

                Text(repeatingUI.dayRightString)
                        .font(.system(size: 14, weight: .light))
                        .foregroundColor(.secondary)
            }

            HStack {

                Text(repeatingUI.listText)
                        .lineSpacing(4)
                        .multilineTextAlignment(.leading)
                        .myMultilineText()

                Spacer()

                TriggersListIconsView(triggers: repeatingUI.textFeatures.triggers, fontSize: 15)
            }
                    .padding(.top, 4)
        }
                .padding(.leading, TAB_TASKS_PADDING_HALF_H)
                .padding(.top, 10)
                .padding(.bottom, 10)
                .foregroundColor(.primary)
                .sheetEnv(
                        isPresented: $isEditSheetPresented,
                        content: {
                            RepeatingsFormSheet(
                                    isPresented: $isEditSheetPresented,
                                    editedRepeating: repeatingUI.repeating
                            ) {
                            }
                        }
                )
                .id("\(repeatingUI.repeating.id) \(repeatingUI.repeating.text)") /// #TruncationDynamic
    }
}
