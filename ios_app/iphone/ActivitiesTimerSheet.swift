import SwiftUI
import shared

extension TimetoSheet {

    func showActivitiesTimerSheet(
            isPresented: Binding<Bool>,
            timerContext: ActivityTimerSheetVM.TimerContext?,
            selectedActivity: ActivityModel?,
            onStart: @escaping () -> Void
    ) {
        items.append(
                TimetoSheet__Item(
                        isPresented: isPresented,
                        content: {
                            AnyView(
                                    ActivitiesTimerSheet(
                                            isPresented: isPresented,
                                            timerContext: timerContext,
                                            selectedActivity: selectedActivity
                                    ) {
                                        isPresented.wrappedValue = false
                                        onStart()
                                    }
                                            .cornerRadius(10, onTop: true, onBottom: false)
                            )
                        }
                )
        )
    }
}

private let bgColor = c.sheetBg
private let itemHeight = 46.0
private let topPadding = 2.0
private let bottomPadding = 32.0

private let activityItemEmojiWidth = 30.0
private let activityItemEmojiHPadding = 8.0
private let activityItemPaddingStart = activityItemEmojiWidth + (activityItemEmojiHPadding * 2)

private let myButtonStyle = MyButtonStyle()

private struct ActivitiesTimerSheet: View {

    @State private var vm: ActivitiesTimerSheetVM

    @State private var sheetActivity: ActivityModel?
    @Binding private var isPresented: Bool

    private let timerContext: ActivityTimerSheetVM.TimerContext?
    private let onStart: () -> Void

    init(
            isPresented: Binding<Bool>,
            timerContext: ActivityTimerSheetVM.TimerContext?,
            selectedActivity: ActivityModel?,
            onStart: @escaping () -> Void
    ) {
        _isPresented = isPresented
        self.timerContext = timerContext
        self.onStart = onStart

        _vm = State(initialValue: ActivitiesTimerSheetVM(timerContext: timerContext))
        _sheetActivity = State(initialValue: selectedActivity)
    }

    var body: some View {

        // todo If inside VMView twitch on open sheet
        let sheetHeight = max(
            /// If height too small - invalid UI
                ActivityTimerSheet.RECOMMENDED_HEIGHT,
            /// Do not be afraid of too much height because the native sheet will cut
                bottomPadding * topPadding + (DI.activitiesSorted.count.toDouble() * itemHeight)
        )

        VMView(vm: vm) { state in

            ZStack {

                if let sheetActivity = sheetActivity {
                    ActivityTimerSheet(
                            activity: sheetActivity,
                            isPresented: $isPresented,
                            timerContext: timerContext,
                            onStart: {
                                onStart()
                            }
                    )
                } else {

                    ScrollView {

                        VStack {

                            Padding(vertical: topPadding)

                            ForEach(state.allActivities, id: \.activity.id) { activityUI in

                                Button(
                                        action: {
                                            sheetActivity = activityUI.activity
                                        },
                                        label: {

                                            ZStack(alignment: .bottom) { // .bottom for divider

                                                HStack {

                                                    Text(activityUI.activity.emoji)
                                                            .frame(width: activityItemEmojiWidth)
                                                            .padding(.horizontal, activityItemEmojiHPadding)
                                                            .font(.system(size: 22))

                                                    Text(activityUI.listText)
                                                            .foregroundColor(.primary)
                                                            .truncationMode(.tail)
                                                            .lineLimit(1)

                                                    Spacer()

                                                    ForEach(activityUI.timerHints, id: \.seconds) { hintUI in
                                                        let isPrimary = hintUI.isPrimary
                                                        Button(
                                                                action: {
                                                                    hintUI.startInterval {
                                                                        onStart()
                                                                    }
                                                                },
                                                                label: {
                                                                    Text(hintUI.text)
                                                                            .font(.system(size: isPrimary ? 14 : 15, weight: isPrimary ? .medium : .light))
                                                                            .foregroundColor(isPrimary ? .white : .blue)
                                                                            .padding(.leading, 6)
                                                                            .padding(.trailing, isPrimary ? 6 : 2)
                                                                            .padding(.top, 3)
                                                                            .padding(.bottom, 3)
                                                                            .background(isPrimary ? .blue : .clear)
                                                                            .cornerRadius(99)
                                                                            .padding(.leading, isPrimary ? 4 : 0)
                                                                }
                                                        )
                                                                .buttonStyle(.borderless)
                                                    }
                                                }
                                                        .padding(.trailing, 14)
                                                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)

                                                if state.allActivities.last != activityUI {
                                                    DividerSheetBg()
                                                            .padding(.leading, activityItemPaddingStart)
                                                }
                                            }
                                                    .frame(alignment: .bottom)
                                        }
                                )
                            }
                                    .buttonStyle(myButtonStyle)

                            Padding(vertical: bottomPadding)
                        }
                    }
                }
            }
        }
                .frame(maxHeight: sheetHeight)
                .background(bgColor)
                .listStyle(.plain)
                .listSectionSeparatorTint(.clear)
                .onDisappear {
                    sheetActivity = nil
                }
    }
}

private struct MyButtonStyle: ButtonStyle {

    func makeBody(configuration: Self.Configuration) -> some View {
        configuration
                .label
                .frame(height: itemHeight)
                .background(configuration.isPressed ? c.dividerFg : bgColor)
    }
}

private struct ActivityTimerSheet: View {

    @State private var vm: ActivityTimerSheetVM

    @Binding private var isPresented: Bool
    private let onStart: () -> ()

    @State private var formTimeItemsIdx: Int32 = 0.toInt32()

    static let RECOMMENDED_HEIGHT = 400.0 // Approximately

    init(
            activity: ActivityModel,
            isPresented: Binding<Bool>,
            timerContext: ActivityTimerSheetVM.TimerContext?,
            onStart: @escaping () -> ()
    ) {
        self._isPresented = isPresented
        self.onStart = onStart
        _vm = State(initialValue: ActivityTimerSheetVM(activity: activity, timerContext: timerContext))
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            HStack(spacing: 4) {

                Button(
                        action: { isPresented.toggle() },
                        label: { Text("Cancel") }
                )

                Spacer()

                Text(state.title)
                        .font(.title2)
                        .fontWeight(.semibold)
                        .multilineTextAlignment(.center)

                Spacer()

                Button(
                        action: {
                            vm.start {
                                onStart()
                            }
                        },
                        label: {
                            Text("Start")
                                    .fontWeight(.bold)
                        }
                )
            }
                    .padding(.horizontal, 25)
                    .padding(.top, 24)

            // Plus padding from picker
            if let note = state.note {
                Text(note)
                        .font(.system(size: 14))
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.top, 6)
            }

            Spacer()


            Picker(
                    "Time",
                    selection: $formTimeItemsIdx
            ) {
                ForEach(state.timeItems, id: \.idx) { item in
                    Text(item.title)
                }
            }
                    .onChange(of: formTimeItemsIdx) { newValue in
                        vm.setFormTimeItemIdx(newIdx: newValue)
                    }
                    .onAppear {
                        formTimeItemsIdx = state.formTimeItemIdx
                    }
                    .pickerStyle(.wheel)
                    .foregroundColor(.primary)
                    .padding(.bottom, state.note != nil ? 30 : 5)

            Spacer()
        }
    }
}
