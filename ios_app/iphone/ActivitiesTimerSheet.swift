import SwiftUI
import shared

extension NativeSheet {

    func showActivitiesTimerSheet(
            timerContext: ActivityTimerSheetVM.TimerContext?,
            withMenu: Bool,
            selectedActivity: ActivityModel?,
            onStart: @escaping (_ isPresented: Binding<Bool>) -> Void
    ) {
        items.append(
                NativeSheet__Item(
                        content: { isPresented in
                            AnyView(
                                    ActivitiesTimerSheet(
                                            isPresented: isPresented,
                                            timerContext: timerContext,
                                            withMenu: withMenu,
                                            selectedActivity: selectedActivity
                                    ) {
                                        isPresented.wrappedValue = false
                                        onStart(isPresented)
                                    }
                            )
                        }
                )
        )
    }
}

extension TimetoSheet {

    func showActivitiesTimerSheet(
            isPresented: Binding<Bool>,
            timerContext: ActivityTimerSheetVM.TimerContext?,
            withMenu: Bool,
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
                                            withMenu: withMenu,
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
private let listItemHeight = 46.0
private let topContentPadding = 8.0
private let bottomContentPadding = 36.0

private let activityItemEmojiWidth = 30.0
private let activityItemEmojiHPadding = 8.0
private let activityItemPaddingStart = activityItemEmojiWidth + (activityItemEmojiHPadding * 2)

private let secondaryFontSize = 16.0
private let secondaryFontWeight: Font.Weight = .light
private let timerHintHPadding = 5.0
private let listEngPadding = 8.0

private let myButtonStyle = MyButtonStyle()

private struct ActivitiesTimerSheet: View {

    @EnvironmentObject private var nativeSheet: NativeSheet

    @State private var vm: ActivitiesTimerSheetVM

    @State private var sheetActivity: ActivityModel?
    @Binding private var isPresented: Bool

    @State private var sheetHeight: Double

    @State private var isChartPresented = false
    @State private var isHistoryPresented = false
    @State private var isEditActivitiesPresented = false

    private let timerContext: ActivityTimerSheetVM.TimerContext?
    private let withMenu: Bool
    private let onStart: () -> Void

    init(
            isPresented: Binding<Bool>,
            timerContext: ActivityTimerSheetVM.TimerContext?,
            withMenu: Bool,
            selectedActivity: ActivityModel?,
            onStart: @escaping () -> Void
    ) {
        _isPresented = isPresented
        self.timerContext = timerContext
        self.withMenu = withMenu
        self.onStart = onStart

        let vm = ActivitiesTimerSheetVM(timerContext: timerContext)
        _vm = State(initialValue: vm)
        _sheetActivity = State(initialValue: selectedActivity)

        let vmState = vm.state.value as! ActivitiesTimerSheetVM.State
        _sheetHeight = State(initialValue: calcSheetHeight(
                activitiesCount: vmState.allActivities.count,
                withMenu: withMenu
        ))
    }

    var body: some View {

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

                            Padding(vertical: topContentPadding)

                            ForEach(state.allActivities, id: \.activity.id) { activityUI in

                                Button(
                                        action: {
                                            // onTapGesture() / onLongPressGesture()
                                        },
                                        label: {

                                            ZStack(alignment: .bottomLeading) { // divider + isActive

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

                                                    TimerHintsView(
                                                            timerHintsUI: activityUI.timerHints,
                                                            hintHPadding: timerHintHPadding,
                                                            fontSize: secondaryFontSize,
                                                            fontWeight: secondaryFontWeight,
                                                            onStart: {
                                                                onStart()
                                                            }
                                                    )
                                                }
                                                        .padding(.trailing, listEngPadding)
                                                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)

                                                if state.allActivities.last != activityUI {
                                                    SheetDividerBg()
                                                            .padding(.leading, activityItemPaddingStart)
                                                }

                                                if activityUI.isActive {
                                                    ZStack {}
                                                            .frame(width: 8, height: listItemHeight - 2)
                                                            .background(roundedShape.fill(c.blue))
                                                            .offset(x: -4, y: -1)
                                                }
                                            }
                                                    /// Ordering is important
                                                    .contentShape(Rectangle()) // TRICK for tap gesture
                                                    .onTapGesture {
                                                        sheetActivity = activityUI.activity
                                                    }
                                                    .onLongPressGesture(minimumDuration: 0.1) {
                                                        nativeSheet.show { isActivityFormPresented in
                                                            ActivityFormSheet(
                                                                    isPresented: isActivityFormPresented,
                                                                    activity: activityUI.activity
                                                            ) {}
                                                        }
                                                    }
                                                    //////
                                                    .frame(alignment: .bottom)
                                        }
                                )
                            }
                                    .buttonStyle(myButtonStyle)

                            if withMenu {

                                HStack {

                                    ChartHistoryButton(text: "Chart", iconName: "chart.pie", iconSize: 18) {
                                        isChartPresented = true
                                    }
                                            .padding(.leading, 13)
                                            .padding(.trailing, 12)
                                            .sheetEnv(isPresented: $isChartPresented) {
                                                VStack {

                                                    ChartView()
                                                            .padding(.top, 15)

                                                    Button(
                                                            action: { isChartPresented.toggle() },
                                                            label: { Text("close").fontWeight(.light) }
                                                    )
                                                            .padding(.bottom, 4)
                                                }
                                            }

                                    ChartHistoryButton(text: "History", iconName: "list.bullet.rectangle", iconSize: 18) {
                                        isHistoryPresented = true
                                    }
                                            .sheetEnv(isPresented: $isHistoryPresented) {
                                                ZStack {
                                                    c.bg.edgesIgnoringSafeArea(.all)
                                                    HistoryView(isHistoryPresented: $isHistoryPresented)
                                                }
                                                        // todo
                                                        .interactiveDismissDisabled()
                                            }

                                    Spacer()

                                    Button(
                                            action: { isEditActivitiesPresented = true },
                                            label: {
                                                Text("Edit")
                                                        .font(.system(size: secondaryFontSize, weight: secondaryFontWeight))
                                                        .padding(.trailing, timerHintHPadding)
                                            }
                                    )
                                            .padding(.trailing, listEngPadding)
                                            .sheetEnv(
                                                    isPresented: $isEditActivitiesPresented
                                            ) {
                                                EditActivitiesSheet(
                                                        isPresented: $isEditActivitiesPresented
                                                )
                                            }
                                }
                                        .frame(height: listItemHeight)
                            }

                            Padding(vertical: bottomContentPadding)
                        }
                    }
                }
            }
                    .onChange(of: state.allActivities.count) { newCount in
                        sheetHeight = calcSheetHeight(
                                activitiesCount: newCount,
                                withMenu: withMenu
                        )
                    }
        }
                .frame(maxHeight: sheetHeight)
                .background(bgColor)
                .listStyle(.plain)
                .listSectionSeparatorTint(.clear)
                .onDisappear {
                    sheetActivity = nil
                }
                .presentationDetentsHeightIf16(sheetHeight, withDragIndicator: true)
                .ignoresSafeArea()
    }
}

private func calcSheetHeight(
        activitiesCount: Int,
        withMenu: Bool
) -> Double {
    let contentHeight = (listItemHeight * activitiesCount.toDouble()) +
                        (withMenu ? listItemHeight : 0.0) + // Buttons
                        topContentPadding +
                        bottomContentPadding
    return max(
            ActivityTimerSheet.RECOMMENDED_HEIGHT, // Invalid UI if height too small
            contentHeight // Do not be afraid of too much height because the native sheet will cut
    )
}

private struct MyButtonStyle: ButtonStyle {

    func makeBody(configuration: Self.Configuration) -> some View {
        configuration
                .label
                .frame(height: listItemHeight)
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

            ZStack {

                VStack {

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

                VStack {

                    Spacer()

                    HStack {
                        TimerHintsView(
                                timerHintsUI: state.timerHints,
                                hintHPadding: 8.0,
                                fontSize: secondaryFontSize,
                                fontWeight: secondaryFontWeight,
                                onStart: {
                                    onStart()
                                }
                        )
                    }
                            .padding(.bottom, 8)
                }
                        .safeAreaPadding(.bottom)
            }
        }
    }
}

private struct ChartHistoryButton: View {

    let text: String
    let iconName: String
    let iconSize: CGFloat
    let onClick: () -> Void

    var body: some View {
        Button(
                action: { onClick() },
                label: {
                    HStack {
                        Image(systemName: iconName)
                                .font(.system(size: iconSize, weight: .thin))
                                .padding(.trailing, 3 + onePx)
                        Text(text)
                                .font(.system(size: secondaryFontSize, weight: secondaryFontWeight))
                    }
                }
        )
    }
}
