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
                                let uiRepeatings = state.uiRepeatings.reversed()
                                ForEach(uiRepeatings, id: \.repeating.id) { uiRepeating in
                                    RepeatingsView__ItemView(
                                            uiRepeating: uiRepeating,
                                            withTopDivider: uiRepeatings.first != uiRepeating
                                    )
                                }
                            }
                                    .background(Color(.mySecondaryBackground))
                                    .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
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

    private let uiRepeating: RepeatingsListVM.UiRepeating

    @State private var isEditSheetPresented = false

    private let withTopDivider: Bool

    init(
            uiRepeating: RepeatingsListVM.UiRepeating,
            withTopDivider: Bool
    ) {
        self.uiRepeating = uiRepeating
        self.withTopDivider = withTopDivider
    }

    var body: some View {
        MyListSwipeToActionItem(
                withTopDivider: withTopDivider,
                deletionHint: uiRepeating.listText,
                deletionConfirmationNote: uiRepeating.deletionNote,
                onEdit: {
                    isEditSheetPresented = true
                },
                onDelete: {
                    withAnimation {
                        // WARNING WTF! // todo is it actual after migration to KMM?
                        // Without async the getPeriod() calls with NPE on typeId
                        myAsyncAfter(0.1) {
                            uiRepeating.delete_()
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
                Text(uiRepeating.dayLeftString)
                        .font(.system(size: 14, weight: .light))
                        .foregroundColor(.secondary)

                Spacer()

                Text(uiRepeating.dayRightString)
                        .font(.system(size: 14, weight: .light))
                        .foregroundColor(.secondary)
            }
                    .padding(.leading, DEF_LIST_H_PADDING)
                    .padding(.trailing, DEF_LIST_H_PADDING)

            HStack {
                Text(uiRepeating.listText)
                        .myMultilineText()
                Spacer(minLength: 0)
            }
                    .padding(.top, 4)
                    .padding(.leading, DEF_LIST_H_PADDING)
                    .padding(.trailing, DEF_LIST_H_PADDING)

            TriggersView__List(triggers: uiRepeating.triggers)
                    .padding(.top, uiRepeating.triggers.isEmpty ? 0 : 8)
        }
                .padding(.top, 10)
                .padding(.bottom, 10)
                .foregroundColor(.primary)
                .sheetEnv(
                        isPresented: $isEditSheetPresented,
                        content: {
                            RepeatingsFormSheet(
                                    isPresented: $isEditSheetPresented,
                                    editedRepeating: uiRepeating.repeating
                            ) {
                            }
                        }
                )
                .id("\(uiRepeating.repeating.id) \(uiRepeating.repeating.text)") /// #TruncationDynamic
    }

    private struct MyButtonStyle: ButtonStyle {
        func makeBody(configuration: Self.Configuration) -> some View {
            configuration.label.background(configuration.isPressed ? Color(.systemGray5) : Color(.mySecondaryBackground))
        }
    }
}
