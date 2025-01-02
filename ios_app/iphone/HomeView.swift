import SwiftUI
import Combine
import shared

let HomeView__BOTTOM_NAVIGATION_HEIGHT = 56.0
let HomeView__PRIMARY_FONT_SIZE = 18.0

// MTG - Main Tasks & Goals
let HomeView__MTG_ITEM_HEIGHT = 38.0
private let mtgCircleHPadding = 7.0
private let mtgCircleHeight = 24.0
private let mtgCircleFontSize = 15.0
private let mtgCircleFontWeight: Font.Weight = .semibold

private let menuTimeFont = buildTimerFont(size: 10)

private let timerFont1 = buildTimerFont(size: 44)
private let timerFont2 = buildTimerFont(size: 38)
private let timerFont3 = buildTimerFont(size: 30)

struct HomeView: View {

    @State private var vm = HomeVm()

    @EnvironmentObject private var nativeSheet: NativeSheet
    @EnvironmentObject private var navigation: Navigation

    @State private var isPurpleAnim = true

    static var lastInstance: HomeView? = nil

    @State private var triggersChecklist: ChecklistDb?
    @State private var isTriggersChecklistPresented = false
    @State private var isSettingsSheetPresented = false

    private let shortcutPublisher: AnyPublisher<ShortcutDb, Never> = Utils_kmpKt.uiShortcutFlow.toPublisher()
    private let checklistPublisher: AnyPublisher<ChecklistDb, Never> = Utils_kmpKt.uiChecklistFlow.toPublisher()

    var body: some View {

        VMView(vm: vm, stack: .ZStack(alignment: .bottom)) { state in

            /// # PROVOKE_STATE_UPDATE
            EmptyView().id("MainView checklist \(triggersChecklist?.id ?? 0)")

            Color.black.edgesIgnoringSafeArea(.all)
                .animateVmValue(value: state.isPurple, state: $isPurpleAnim)

            VStack {

                let timerData = state.timerData
                let noteColor = timerData.noteColor.toColor()
                let timerColor = timerData.timerColor.toColor()
                let timerControlsColor = state.timerData.controlsColor.toColor()

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

                ZStack(alignment: .top) {

                    TimerDataNoteText(
                        text: state.timerData.note,
                        color: noteColor
                    )

                    HStack {

                        VStack {

                            TimerDataNoteText(text: " ", color: c.transparent)

                            Button(
                                action: {
                                    vm.toggleIsPurple()
                                },
                                label: {

                                    ZStack {

                                        TimerDataTimerText(
                                            text: " ",
                                            font: timerFont,
                                            color: c.transparent
                                        )

                                        Image(systemName: "info")
                                            .foregroundColor(timerControlsColor)
                                            .font(.system(size: 23, weight: .thin))
                                            .frame(maxWidth: .infinity)
                                    }
                                }
                            )
                        }

                        Button(
                            action: {
                                state.timerData.togglePomodoro()
                            },
                            label: {

                                VStack {

                                    TimerDataNoteText(text: " ", color: c.transparent)

                                    TimerDataTimerText(
                                        text: timerData.timerText,
                                        font: timerFont,
                                        color: timerColor
                                    )
                                }
                            }
                        )

                        VStack {

                            TimerDataNoteText(text: " ", color: c.transparent)

                            Button(
                                action: {
                                    state.timerData.prolong()
                                },
                                label: {

                                    ZStack {

                                        TimerDataTimerText(
                                            text: " ",
                                            font: timerFont,
                                            color: c.transparent
                                        )

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
                                }
                            )
                        }
                    }
                }
                .padding(.bottom, 11)

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
                                        daytimeUi: infoUi.untilDaytimeUi,
                                        onPick: { daytimePickerUi in
                                            infoUi.setUntilDaytime(daytimeUi: daytimePickerUi)
                                            vm.toggleIsPurple()

                                        },
                                        onRemove: {}
                                    )
                                    .presentationDetents([.medium])
                                    .presentationDragIndicator(.visible)
                                }
                            }
                        )

                        TimerInfoButton(
                            text: infoUi.timerText,
                            color: timerColor,
                            onClick: {
                                nativeSheet.showActivityTimerSheet(
                                    activity: state.activeActivityDb,
                                    timerContext: state.timerData.infoUi.timerContext,
                                    hideOnStart: true,
                                    onStart: {}
                                )
                            }
                        )

                        TimerInfoButton(
                            text: "?",
                            color: timerColor,
                            onClick: {
                                navigation.path.append(.readme(defaultItem: .pomodoro))
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
                                    navigation.path.append(.readme(defaultItem: .basics))
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
                            NavigationLink(.whatsNew) {
                                Text(whatsNewMessage)
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 8)
                                    .font(.system(size: 17, weight: .medium))
                                    .background(roundedShape.fill(.red))
                                    .padding(.top, 8)
                            }
                        }

                        let isMainTasksExists = !state.mainTasks.isEmpty

                        GeometryReader { geometry in

                            let _ = vm.upListsContainerSize(
                                totalHeight: Float(geometry.size.height),
                                itemHeight: Float(HomeView__MTG_ITEM_HEIGHT)
                            )

                            VStack {

                                if let checklistDb = checklistDb {
                                    VStack {
                                        ChecklistView(
                                            checklistDb: checklistDb,
                                            onDelete: {},
                                            maxLines: 1,
                                            bottomPadding: 0
                                        )
                                    }
                                    .frame(height: CGFloat(state.listsSizes.checklist))
                                    .id("home_checklist_id_\(checklistDb.id)") // Force update on change
                                }

                                if isMainTasksExists {
                                    MainTasksView(
                                        tasks: state.mainTasks
                                    )
                                        .frame(height: CGFloat(state.listsSizes.mainTasks))
                                }

                                Spacer()
                            }
                        }

                        ForEachIndexed(
                            state.goalBarsUi,
                            content: { idx, goalBarUi in

                                Button(
                                    action: {
                                        goalBarUi.startInterval()
                                    },
                                    label: {
                                        
                                        ZStack {

                                            ZStack {

                                                GeometryReader { geometry in
                                                    VStack {
                                                        ZStack {
                                                        }
                                                        .frame(maxHeight: .infinity)
                                                        .frame(width: geometry.size.width * Double(goalBarUi.ratio))
                                                        .background(goalBarUi.bgColor.toColor())
                                                        Spacer()
                                                    }
                                                }
                                                .frame(width: .infinity)
                                                .clipShape(roundedShape)

                                                HStack {

                                                    Text(goalBarUi.textLeft)
                                                        .padding(.leading, mtgCircleHPadding)
                                                        .foregroundColor(c.white)
                                                        .font(.system(size: mtgCircleFontSize, weight: mtgCircleFontWeight))

                                                    Spacer()

                                                    Text(goalBarUi.textRight)
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
                                        .offset(y: 1)
                                    }
                                )
                            }
                        )

                        Padding(vertical: 10.0)
                    }
                    .padding(.bottom, HomeView__BOTTOM_NAVIGATION_HEIGHT)

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
                                .frame(height: HomeView__BOTTOM_NAVIGATION_HEIGHT)
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

                        VStack(alignment: .center) {

                            Text(state.menuTime)
                                .foregroundColor(c.homeMenuTime)
                                .font(menuTimeFont)
                                .padding(.top, 3)
                                .padding(.bottom, 7)

                            HStack {

                                let batteryUi = state.batteryUi
                                let batteryTextColor = batteryUi.colorRgba.toColor()

                                Image(systemName: "bolt.fill")
                                    .foregroundColor(batteryTextColor)
                                    .font(.system(size: 12, weight: batteryUi.isHighlighted ? .regular : .ultraLight))

                                Text(batteryUi.text)
                                    .foregroundColor(batteryTextColor)
                                    .font(.system(size: 13, weight: batteryUi.isHighlighted ? .bold : .regular))

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
                        .frame(maxWidth: .infinity)
                        .background(state.isTasksVisible ? Color(.systemGray5) : .black)
                        .cornerRadius(10, onTop: true, onBottom: true)
                    }
                )

                Button(
                    action: {
                        isSettingsSheetPresented = true
                    },
                    label: {
                        VStack {
                            Spacer()
                            Image(systemName: "ellipsis.circle")
                                .frame(height: HomeView__BOTTOM_NAVIGATION_HEIGHT)
                                .foregroundColor(c.homeFontSecondary)
                                .font(.system(size: 30, weight: .thin))
                                .frame(maxWidth: .infinity)
                        }
                    }
                )
                .sheetEnv(isPresented: $isSettingsSheetPresented) {
                    SettingsScreen()
                }
            }
            .frame(width: .infinity, height: HomeView__BOTTOM_NAVIGATION_HEIGHT)
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

    let tasks: [HomeVm.MainTask]

    private let LIST_BOTTOM_ITEM_ID = "bottom_id"

    var body: some View {

        GeometryReader { geometry in

            ScrollViewReader { scrollProxy in

                ScrollView(showsIndicators: false) {

                    VStack {

                        Spacer()

                        ForEach(tasks.reversed(), id: \.self.taskUi.taskDb.id) { mainTask in
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

    let mainTask: HomeVm.MainTask

    @EnvironmentObject private var nativeSheet: NativeSheet

    var body: some View {

        Button(
            action: {
                mainTask.taskUi.taskDb.startIntervalForUI(
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
                            .padding(.trailing, mainTask.taskUi.tf.paused != nil ? 9 : 8)
                    }

                    if mainTask.taskUi.tf.paused != nil {
                        ZStack {
                            Image(systemName: "pause")
                                .foregroundColor(c.white)
                                .font(.system(size: 12, weight: .black))
                        }
                        .frame(width: mtgCircleHeight, height: mtgCircleHeight)
                        .background(roundedShape.fill(c.green))
                        .padding(.trailing, 8)
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

///

private struct TimerDataNoteText: View {

    let text: String
    let color: Color

    var body: some View {

        Text(text)
            .font(.system(size: 20, weight: .bold))
            .foregroundColor(color)
            .multilineTextAlignment(.center)
            .padding(.horizontal, H_PADDING)
            .padding(.bottom, 9)
            .lineLimit(1)
    }
}

private struct TimerDataTimerText: View {

    let text: String
    let font: Font
    let color: Color

    var body: some View {

        Text(text)
            .font(font)
            .foregroundColor(color)
            .lineLimit(1)
            .fixedSize()
    }
}
