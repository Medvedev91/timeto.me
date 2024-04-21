import SwiftUI
import Combine
import shared

let HomeView__BOTTOM_NAVIGATION_HEIGHT = 56.0
let HomeView__PRIMARY_FONT_SIZE = 18.0

private let menuIconSize = HomeView__BOTTOM_NAVIGATION_HEIGHT

// MTG - Main Tasks & Goals
private let mtgItemHeight = 38.0
private let mtgCircleHPadding = 7.0
private let mtgCircleHeight = 24.0
private let mtgCircleFontSize = 15.0
private let mtgCircleFontWeight: Font.Weight = .semibold

private let taskCountsHeight = 36.0

private let mainTasksContentTopPadding = 4.0

private let menuTimeFont = buildTimerFont(size: 10)

private let timerFont1 = buildTimerFont(size: 44)
private let timerFont2 = buildTimerFont(size: 38)
private let timerFont3 = buildTimerFont(size: 30)

private let navAndTasksTextHeight = HomeView__BOTTOM_NAVIGATION_HEIGHT + taskCountsHeight

struct HomeView: View {

    @State private var vm = HomeVM()

    @State private var isSettingsSheetPresented = false

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
                let timerColor = timerData.color.toColor()
                let timerButtonsColor = state.timerButtonsColor.toColor()

                Text(state.title)
                    .font(.system(size: 21, weight: .semibold))
                    .foregroundColor(timerColor)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 20)

                HStack {

                    Button(
                        action: {
                            vm.pauseTask()
                        },
                        label: {
                            Image(systemName: "pause")
                                .foregroundColor(timerButtonsColor)
                                .font(.system(size: 22, weight: .thin))
                                .frame(maxWidth: .infinity)
                                .frame(height: timerHeight)
                        }
                    )

                    Button(
                        action: {
                            vm.toggleIsPurple()
                        },
                        label: {
                            let timerFont: Font = {
                                let len = timerData.title.count
                                if len <= 5 {
                                    return timerFont1
                                }
                                if len <= 7 {
                                    return timerFont2
                                }
                                return timerFont3
                            }()
                            Text(timerData.title)
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
                            state.timerData.restart()
                        },
                        label: {
                            Text(state.timerData.restartText)
                                .font(.system(size: 22, weight: .thin))
                                .foregroundColor(timerButtonsColor)
                                .frame(maxWidth: .infinity)
                                .frame(height: timerHeight)
                        }
                    )
                    .offset(x: 2)
                }
                .padding(.top, 13)
                .padding(.bottom, 14)

                if state.isPurple {

                    HStack {

                        TimerHintsView(
                            timerHintsUI: state.timerHints,
                            hintHPadding: 10.0,
                            fontSize: 22.0,
                            fontWeight: .thin,
                            fontColor: timerColor,
                            onStart: {}
                        )

                        Button(
                            action: {
                                nativeSheet.showActivityTimerSheet(
                                    activity: state.activity,
                                    timerContext: state.timerButtonExpandSheetContext,
                                    hideOnStart: true,
                                    onStart: {}
                                )
                            },
                            label: {
                                Image(systemName: "chevron.down.circle.fill")
                                    .foregroundColor(timerColor)
                                    .font(.system(size: 22, weight: .regular))
                            }
                        )
                        .padding(.leading, 9)
                        .offset(y: -onePx)
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
                                    nativeSheet.show { isReadmePresented in
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
                                    (mtgItemHeight * state.mainTasks.count.toDouble().limitMax(5.45))
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
                                .frame(height: mtgItemHeight, alignment: .center)
                            }
                        )

                        Padding(vertical: 16.0)
                    }
                    .padding(.bottom, navAndTasksTextHeight)

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
                                        .padding(.trailing, 1)
                                }
                                .padding(.top, 2)
                                .padding(.bottom, 1)
                                .padding(.leading, 3)
                                .padding(.trailing, 4)
                                .background(roundedShape.fill(state.batteryBackground.toColor()))
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
                        isSettingsSheetPresented = true
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
            .frame(width: .infinity, height: state.isTasksVisible ? HomeView__BOTTOM_NAVIGATION_HEIGHT : navAndTasksTextHeight)
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
        .sheetEnv(
            isPresented: $isSettingsSheetPresented
        ) {
            SettingsSheet(isPresented: $isSettingsSheetPresented)
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
                        Image(systemName: "pause")
                            .foregroundColor(c.homeFontSecondary)
                            .font(.system(size: 12, weight: .black))
                            .padding(.trailing, 5)
                    }

                    Text(mainTask.text)
                        .font(.system(size: HomeView__PRIMARY_FONT_SIZE))
                        .foregroundColor(Color.white)
                        .padding(.trailing, 4)

                    Spacer()

                    if let timeUI = mainTask.timeUI {
                        Text(timeUI.note)
                            .offset(y: 1)
                            .foregroundColor(timeUI.noteColor.toColor())
                            .font(.system(size: 14, weight: .light))
                    }
                }
                .frame(height: mtgItemHeight)
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
