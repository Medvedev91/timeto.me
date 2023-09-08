import SwiftUI
import shared

extension NativeSheet {

    func showActivitiesTimerSheet(
            timerContext: ActivityTimerSheetVM.TimerContext?,
            withMenu: Bool,
            onStart: @escaping () -> Void
    ) {
        self.show { isPresented in
            ActivitiesTimerSheet(
                    isPresented: isPresented,
                    timerContext: timerContext,
                    withMenu: withMenu
            ) {
                isPresented.wrappedValue = false
                onStart()
            }
        }
    }
}

private let minSheetHeight = 400.0 // For valid timer picker

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
            onStart: @escaping () -> Void
    ) {
        _isPresented = isPresented
        self.timerContext = timerContext
        self.withMenu = withMenu
        self.onStart = onStart

        let vm = ActivitiesTimerSheetVM(timerContext: timerContext)
        _vm = State(initialValue: vm)

        let vmState = vm.state.value as! ActivitiesTimerSheetVM.State
        _sheetHeight = State(initialValue: calcSheetHeight(
                activitiesCount: vmState.allActivities.count,
                withMenu: withMenu
        ))
    }

    var body: some View {

        VMView(vm: vm) { state in

            ZStack {

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
                                                    nativeSheet.showActivityTimerSheet(
                                                            activity: activityUI.activity,
                                                            timerContext: timerContext,
                                                            hideOnStart: false,
                                                            onStart: {
                                                                isPresented = false
                                                            }
                                                    )
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
    // Do not be afraid of too much height because the native sheet will cut
    return max(minSheetHeight, contentHeight)
}

private struct MyButtonStyle: ButtonStyle {

    func makeBody(configuration: Self.Configuration) -> some View {
        configuration
                .label
                .frame(height: listItemHeight)
                .background(configuration.isPressed ? c.dividerFg : bgColor)
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
