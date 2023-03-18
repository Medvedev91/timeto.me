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

                        VStack(spacing: 0) {

                            Spacer()

                            VStack(spacing: 0) {
                                let repeatingsUI = state.repeatingsUI.reversed()
                                ForEach(repeatingsUI, id: \.repeating.id) { repeatingUI in
                                    let isFirst = repeatingsUI.first == repeatingUI
                                    MyListView__ItemView(
                                            isFirst: isFirst,
                                            isLast: repeatingsUI.last == repeatingUI,
                                            withTopDivider: !isFirst,
                                            outerPaddingStart: 0,
                                            outerPaddingEnd: 0
                                    ) {
                                        RepeatingsView__ItemView(repeatingUI: repeatingUI)
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
                                                .background(
                                                        RoundedRectangle(cornerRadius: 8, style: .continuous)
                                                                .fill(.blue)
                                                )
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
                    .padding(.leading, MyListView.PADDING_OUTER_HORIZONTAL)
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
        }
    }

    private var safeView: some View {

        VStack(spacing: 0) {

            HStack {
                Text(repeatingUI.dayLeftString)
                        .font(.system(size: 14, weight: .light))
                        .foregroundColor(.secondary)

                Spacer()

                Text(repeatingUI.dayRightString)
                        .font(.system(size: 14, weight: .light))
                        .foregroundColor(.secondary)
            }
                    .padding(.leading, DEF_LIST_H_PADDING)
                    .padding(.trailing, DEF_LIST_H_PADDING)

            HStack {
                Text(repeatingUI.listText)
                        .myMultilineText()
                Spacer(minLength: 0)
            }
                    .padding(.top, 4)
                    .padding(.leading, DEF_LIST_H_PADDING)
                    .padding(.trailing, DEF_LIST_H_PADDING)

            TextFeaturesTriggersView(textFeatures: repeatingUI.textFeatures)
                    .padding(.top, repeatingUI.textFeatures.triggers.isEmpty ? 0 : 8)
        }
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

    private struct MyButtonStyle: ButtonStyle {
        func makeBody(configuration: Self.Configuration) -> some View {
            configuration.label.background(configuration.isPressed ? Color(.systemGray5) : Color(.mySecondaryBackground))
        }
    }
}
