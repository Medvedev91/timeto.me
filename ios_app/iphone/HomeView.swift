import SwiftUI
import Combine
import shared

let HomeView__BOTTOM_NAVIGATION_HEIGHT = 56.0
let HomeView__PRIMARY_FONT_SIZE = 18.0

private let menuIconSize = HomeView__BOTTOM_NAVIGATION_HEIGHT

// MTG - Main Tasks & Goals
let HomeView__MTG_ITEM_HEIGHT = 42.0
private let mtgCircleHPadding = 7.0
private let mtgCircleHeight = 24.0
private let mtgCircleFontSize = 15.0
private let mtgCircleFontWeight: Font.Weight = .semibold

private let mainTasksContentTopPadding = 4.0

private let navigationNoteHeight = 36.0
private let navigationButtonHeight = HomeView__BOTTOM_NAVIGATION_HEIGHT + navigationNoteHeight

private let menuTimeFont = buildTimerFont(size: 10)

private let timerFont1 = buildTimerFont(size: 44)
private let timerFont2 = buildTimerFont(size: 38)
private let timerFont3 = buildTimerFont(size: 30)

struct HomeView: View {

    @State private var vm = HomeVM()

    @EnvironmentObject private var fs: Fs
    @EnvironmentObject private var nativeSheet: NativeSheet

    @State private var isPurpleAnim = true
    @State private var timerHeight = 30.0

    static var lastInstance: HomeView? = nil

    @State private var triggersChecklist: ChecklistDb?
    @State private var isTriggersChecklistPresented = false

    private let shortcutPublisher: AnyPublisher<ShortcutDb, Never> = Utils_kmpKt.uiShortcutFlow.toPublisher()
    private let checklistPublisher: AnyPublisher<ChecklistDb, Never> = Utils_kmpKt.uiChecklistFlow.toPublisher()

    var body: some View {

        VMView(vm: vm, stack: .ZStack(alignment: .bottom)) { state in

            /// # PROVOKE_STATE_UPDATE
            EmptyView().id("MainView checklist \(triggersChecklist?.id ?? 0)")

            Color.black.edgesIgnoringSafeArea(.all)
                .statusBar(hidden: true)
                .animateVmValue(value: state.isPurple, state: $isPurpleAnim)

            VStack {

                let timerData = state.timerData
                let noteColor = timerData.noteColor.toColor()
                let timerColor = timerData.timerColor.toColor()
                let timerControlsColor = state.timerData.controlsColor.toColor()

                Text(state.timerData.note)
                    .font(.system(size: 21, weight: .semibold))
                    .foregroundColor(noteColor)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 20)

                HStack {

                    Button(
                        action: {
                            vm.toggleIsPurple()
                        },
                        label: {
                            Image(systemName: "info")
                                .foregroundColor(timerControlsColor)
                                .font(.system(size: 23, weight: .thin))
                                .frame(maxWidth: .infinity)
                                .frame(height: timerHeight)
                        }
                    )

                    Button(
                        action: {
                            state.timerData.togglePomodoro()
                        },
                        label: {
                            let timerFont: Font = {
                                let len = timerData.timerText.count
                                if len <= 5 {
                                    return timerFont1
                                }
                                if len <= 7 {
                                    return timerFont2
                                }
                                return timerFont3
                            }()
                            Text(timerData.timerText)
                                .font(timerFont)
                                .foregroundColor(timerColor)
                                .lineLimit(1)
                                .fixedSize()
                        }
                    )
                    .background(GeometryReader { geometry -> Color in
                        myAsyncAfter(0.01) {
                            timerHeight = geometry.size.height
                        }
                        return Color.clear
                    })

                    Button(
                        action: {
                            state.timerData.prolong()
                        },
                        label: {
                            ZStack {
                                if let prolongText = timerData.prolongText {
                                    Text(prolongText)
                                        .font(.system(size: 22, weight: .thin))
                                        .foregroundColor(timerControlsColor)
                                } else {
                                    Image(systemName: "plus")
                                        .foregroundColor(timerControlsColor)
                                        .font(.system(size: 22, weight: .thin))
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: timerHeight)
                        }
                    )
                }
                .padding(.top, 13)
                .padding(.bottom, 14)

                if state.isPurple {

                    let infoUi = state.timerData.infoUi

                    HStack {

                        TimerInfoButton(
                            text: infoUi.untilDaytimeUi.text,
                            color: timerColor,
                            onClick: {
                                nativeSheet.show { isTimerPickerPresented in
                                    DaytimePickerSheet(
                                        isPresented: isTimerPickerPresented,
                                        title: infoUi.untilPickerTitle,
                                        doneText: "Start",
                                        daytimeModel: infoUi.untilDaytimeUi,
                                        onPick: { daytimePickerUi in
                                            infoUi.setUntilDaytime(daytimeUi: daytimePickerUi)
                                            vm.toggleIsPurple()

                                        },
                                        onRemove: {}
                                    )
                                    .presentationDetentsMediumIf16()
                                }
                            }
                        )

                        TimerInfoButton(
                            text: infoUi.timerText,
                            color: timerColor,
                            onClick: {
                                nativeSheet.showActivityTimerSheet(
                                    activity: state.activity,
                                    timerContext: state.timerData.infoUi.timerContext,
                                    hideOnStart: true,
                                    onStart: {}
                                )
                            }
                        )
                    }
                    .offset(y: -4)
                }

                ZStack {

                    let checklistDb = state.checklistDb

                    VStack {

                        if let readmeMessage = state.readmeMessage {
                            Button(
                                action: {
                                    vm.onReadmeOpen()
                                    fs.show { isReadmePresented in
                                        ReadmeSheet(isPresented: isReadmePresented)
                                    }
                                },
                                label: {
                                    Text(readmeMessage)
                                        .foregroundColor(c.white)
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 8)
                                        .font(.system(size: 17, weight: .medium))
                                        .background(roundedShape.fill(.red))
                                        .padding(.top, 8)
                                }
                            )
                        }

                        if let whatsNewMessage = state.whatsNewMessage {
                            Button(
                                action: {
                                    nativeSheet.show { isWhatsNewPresented in
                                        WhatsNewSheet(isPresented: isWhatsNewPresented)
                                    }
                                },
                                label: {
                                    Text(whatsNewMessage)
                                        .foregroundColor(c.white)
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 8)
                                        .font(.system(size: 17, weight: .medium))
                                        .background(roundedShape.fill(.red))
                                        .padding(.top, 8)
                                }
                            )
                        }

                        let isMainTasksExists = !state.mainTasks.isEmpty

                        if let checklistDb = checklistDb {
                            VStack {
                                ChecklistView(
                                    checklistDb: checklistDb,
                                    onDelete: {},
                                    bottomPadding: 0
                                )
                                MainDivider()
                            }
                            .id("home_checklist_id_\(checklistDb.id)") // Force update on change
                        }

                        if isMainTasksExists {
                            let listHeight: CGFloat =
                                checklistDb == nil ? .infinity :
                                    mainTasksContentTopPadding +
                                    (HomeView__MTG_ITEM_HEIGHT * state.mainTasks.count.toDouble().limitMax(5.45))
                            MainTasksView(
                                tasks: state.mainTasks
                            )
                                .frame(height: listHeight)
                        }

                        if !isMainTasksExists && checklistDb == nil {
                            Spacer()
                        }

                        ForEachIndexed(
                            state.goalsUI,
                            content: { idx, goalUI in

                                ZStack {

                                    ZStack {

                                        GeometryReader { geometry in
                                            VStack {
                                                ZStack {
                                                }
                                                .frame(maxHeight: .infinity)
                                                .frame(width: geometry.size.width * Double(goalUI.ratio))
                                                .background(goalUI.bgColor.toColor())
                                                Spacer()
                                            }
                                        }
                                        .frame(width: .infinity)
                                        .clipShape(roundedShape)

                                        HStack {

                                            Text(goalUI.textLeft)
                                                .padding(.leading, mtgCircleHPadding)
                                                .foregroundColor(c.white)
                                                .font(.system(size: mtgCircleFontSize, weight: mtgCircleFontWeight))

                                            Spacer()

                                            Text(goalUI.textRight)
                                                .padding(.trailing, mtgCircleHPadding)
                                                .foregroundColor(c.white)
                                                .font(.system(size: mtgCircleFontSize, weight: mtgCircleFontWeight))
                                        }
                                    }
                                    .frame(height: mtgCircleHeight, alignment: .center)
                                    .background(roundedShape.fill(c.homeFg))
                                    .padding(.horizontal, H_PADDING)
                                }
                                .frame(height: HomeView__MTG_ITEM_HEIGHT, alignment: .center)
                            }
                        )

                        Padding(vertical: 16.0)
                    }
                    .padding(.bottom, navigationButtonHeight)

                    if (state.isTasksVisible) {
                        TasksView()
                            .clipped() // Fix list offset on IME open
                            .padding(.bottom, HomeView__BOTTOM_NAVIGATION_HEIGHT)
                    }
                }
            }

            //
            // Navigation

            HStack(alignment: .bottom) {

                Button(
                    action: {
                        nativeSheet.showActivitiesTimerSheet(
                            timerContext: nil,
                            withMenu: true,
                            onStart: {}
                        )
                    },
                    label: {
                        VStack {
                            Spacer()
                            Image(systemName: "timer")
                                .frame(height: menuIconSize)
                                .foregroundColor(c.homeFontSecondary)
                                .font(.system(size: 30, weight: .thin))
                                .frame(maxWidth: .infinity)
                                .frame(alignment: .bottom)
                        }
                    }
                )

                Button(
                    action: {
                        vm.toggleIsTasksVisible()
                    },
                    label: {

                        VStack {

                            if (!state.isTasksVisible) {

                                Text(state.menuNote)
                                    .foregroundColor(c.homeFontSecondary)
                                    .font(.system(size: 15, weight: .regular))
                                    .padding(.top, 4)

                                Spacer()
                            }

                            VStack(alignment: .center) {

                                Text(state.menuTime)
                                    .foregroundColor(c.homeMenuTime)
                                    .font(menuTimeFont)
                                    .padding(.top, 4)
                                    .padding(.bottom, 4)

                                HStack {

                                    let batteryTextColor = state.batteryTextColor.toColor()

                                    Image(systemName: "bolt.fill")
                                        .foregroundColor(batteryTextColor)
                                        .font(.system(size: 12, weight: .ultraLight))

                                    Text(state.batteryText)
                                        .foregroundColor(batteryTextColor)
                                        .font(.system(size: 13, weight: .regular))

                                    Image(systemName: "smallcircle.filled.circle")
                                        .foregroundColor(c.homeFontSecondary)
                                        .font(.system(size: 11 + halfDpCeil, weight: .regular))
                                        .padding(.leading, 6)
                                        .padding(.trailing, 1 + halfDpFloor)

                                    Text(state.menuTasksNote)
                                        .foregroundColor(c.homeFontSecondary)
                                        .font(.system(size: 13, weight: .regular))
                                }
                                .padding(.trailing, 2)
                            }
                            .padding(.top, 2)
                            .frame(height: HomeView__BOTTOM_NAVIGATION_HEIGHT)
                        }
                        .frame(maxWidth: .infinity)
                        .background(state.isTasksVisible ? Color(.systemGray5) : .black)
                        .cornerRadius(10, onTop: true, onBottom: true)
                    }
                )

                Button(
                    action: {
                        fs.show { isFsPresented in
                            SettingsSheet(isPresented: isFsPresented)
                        }
                    },
                    label: {
                        VStack {
                            Spacer()
                            Image(systemName: "ellipsis.circle")
                                .frame(height: menuIconSize)
                                .foregroundColor(c.homeFontSecondary)
                                .font(.system(size: 30, weight: .thin))
                                .frame(maxWidth: .infinity)
                        }
                    }
                )
            }
            .frame(width: .infinity, height: state.isTasksVisible ? HomeView__BOTTOM_NAVIGATION_HEIGHT : navigationButtonHeight)
        }
        .ignoresSafeArea(.keyboard, edges: .bottom)
        .onReceive(shortcutPublisher) { shortcut in
            let swiftURL = URL(string: shortcut.uri)!
            if !UIApplication.shared.canOpenURL(swiftURL) {
                Utils_kmpKt.showUiAlert(message: "Invalid shortcut link", reportApiText: nil)
                return
            }
            UIApplication.shared.open(swiftURL)
        }
        .onReceive(checklistPublisher) { checklist in
            triggersChecklist = checklist
            isTriggersChecklistPresented = true
        }
        .sheetEnv(isPresented: $isTriggersChecklistPresented) {
            if let checklist = triggersChecklist {
                ChecklistSheet(isPresented: $isTriggersChecklistPresented, checklist: checklist)
            }
        }
        .onAppear {
            HomeView.lastInstance = self
        }
    }
}

private struct MainTasksView: View {

    let tasks: [HomeVM.MainTask]

    private let LIST_BOTTOM_ITEM_ID = "bottom_id"

    var body: some View {

        GeometryReader { geometry in

            ScrollViewReader { scrollProxy in

                ScrollView(showsIndicators: false) {

                    VStack {

                        Spacer()

                        ZStack {
                        }
                        .frame(height: mainTasksContentTopPadding)

                        ForEach(tasks.reversed(), id: \.self.task.id) { mainTask in
                            MainTaskItemView(mainTask: mainTask)
                        }

                        ZStack {
                        }
                        .id(LIST_BOTTOM_ITEM_ID)
                    }
                    .frame(minHeight: geometry.size.height)
                }
                .frame(maxWidth: .infinity)
                .onAppear {
                    scrollProxy.scrollTo(LIST_BOTTOM_ITEM_ID)
                }
            }
        }
    }
}

private struct MainTaskItemView: View {

    let mainTask: HomeVM.MainTask

    @EnvironmentObject private var nativeSheet: NativeSheet

    var body: some View {

        Button(
            action: {
                mainTask.task.startIntervalForUI(
                    onStarted: {},
                    activitiesSheet: {
                        nativeSheet.showActivitiesTimerSheet(
                            timerContext: mainTask.timerContext,
                            withMenu: false,
                            onStart: {}
                        )
                    },
                    timerSheet: { activity in
                        nativeSheet.showActivityTimerSheet(
                            activity: activity,
                            timerContext: mainTask.timerContext,
                            hideOnStart: true,
                            onStart: {}
                        )
                    }
                )
            },
            label: {

                HStack {

                    if let timeUI = mainTask.timeUI {
                        Text(timeUI.text)
                            .foregroundColor(.white)
                            .font(.system(size: mtgCircleFontSize, weight: mtgCircleFontWeight))
                            .padding(.horizontal, mtgCircleHPadding)
                            .frame(height: mtgCircleHeight)
                            .background(roundedShape.fill(timeUI.textBgColor.toColor()))
                            .padding(.trailing, 8)
                    }

                    if mainTask.textFeatures.paused != nil {
                        ZStack {
                            Image(systemName: "pause")
                                .foregroundColor(c.white)
                                .font(.system(size: 12, weight: .black))
                        }
                        .frame(width: mtgCircleHeight, height: mtgCircleHeight)
                        .background(roundedShape.fill(c.green))
                        .padding(.trailing, 7)
                    }

                    Text(mainTask.text)
                        .font(.system(size: HomeView__PRIMARY_FONT_SIZE))
                        .foregroundColor(Color.white)
                        .padding(.trailing, 4)

                    Spacer()

                    if let timeUI = mainTask.timeUI {
                        Text(timeUI.note)
                            .foregroundColor(timeUI.noteColor.toColor())
                            .font(.system(size: HomeView__PRIMARY_FONT_SIZE))
                    }
                }
                .frame(height: HomeView__MTG_ITEM_HEIGHT)
                .padding(.horizontal, H_PADDING)
            }
        )
    }
}

private struct MainDivider: View {

    var isVisible = true

    var body: some View {
        DividerBg(isVisible: isVisible)
            .padding(.horizontal, H_PADDING)
    }
}

private struct TimerInfoButton: View {

    let text: String
    let color: Color
    let onClick: () -> Void

    var body: some View {

        Button(
            action: {
                onClick()
            },
            label: {
                Text(text)
                    .font(.system(size: 22, weight: .thin))
                    .foregroundColor(color)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .cornerRadius(99)
            }
        )
        .buttonStyle(.borderless)
    }
}
