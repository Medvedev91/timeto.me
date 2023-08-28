import SwiftUI
import Combine
import shared

let bottomNavigationHeight = 56.0 // todo

private let menuIconSize = bottomNavigationHeight

private let taskCountsHeight = 36.0

private let taskItemHeight = 36.0
private let taskListContentPadding = 4.0

private let menuColor = MainVM.companion.menuColor.toColor()
private let menuTimeColor = MainVM.companion.menuTimeColor.toColor()
private let menuTimeFont = buildTimerFont(size: 10)

private let timerFont1 = buildTimerFont(size: 44)
private let timerFont2 = buildTimerFont(size: 38)
private let timerFont3 = buildTimerFont(size: 30)

private let navAndTasksTextHeight = bottomNavigationHeight + taskCountsHeight

struct MainView: View {

    @State private var vm = MainVM()
    @State private var isTimerButtonExpandPresented = false
    @State private var isTimerActivitiesPresented = false

    @State private var isSettingsSheetPresented = false

    @EnvironmentObject private var timetoSheet: TimetoSheet

    @State private var isPurpleAnim = true
    @State private var timerHeight = 30.0

    static var lastInstance: MainView? = nil

    @State private var triggersChecklist: ChecklistModel?
    @State private var isTriggersChecklistPresented = false

    private let shortcutPublisher: AnyPublisher<ShortcutModel, Never> = UtilsKt.uiShortcutFlow.toPublisher()
    private let checklistPublisher: AnyPublisher<ChecklistModel, Never> = UtilsKt.uiChecklistFlow.toPublisher()

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
                                        .foregroundColor(timerColor)
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
                                    if len <= 5 { return timerFont1 }
                                    if len <= 7 { return timerFont2 }
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
                                myAsyncAfter(0.01) { timerHeight = geometry.size.height }
                                return Color.clear
                            })

                    Button(
                            action: {
                                state.timerData.restart()
                            },
                            label: {
                                Text(state.timerData.restartText)
                                        .font(.system(size: 22, weight: .thin))
                                        .foregroundColor(timerColor)
                                        .frame(maxWidth: .infinity)
                                        .frame(height: timerHeight)
                            }
                    )
                            .offset(x: 2)
                }
                        .padding(.top, 13)

                if state.isPurple {

                    HStack {

                        TimerHintsView(
                                timerHintsUI: state.timerHints,
                                hintHPadding: 10.0,
                                fontSize: 20.0,
                                fontWeight: .regular,
                                fontColor: timerColor,
                                onStart: {}
                        )

                        Button(
                                action: {
                                    timetoSheet.showActivitiesTimerSheet(
                                            isPresented: $isTimerButtonExpandPresented,
                                            timerContext: state.timerButtonExpandSheetContext,
                                            withMenu: false,
                                            selectedActivity: state.activity,
                                            onStart: {
                                                isTimerButtonExpandPresented = false
                                            }
                                    )
                                },
                                label: {
                                    Image(systemName: "chevron.down.circle.fill")
                                            .foregroundColor(timerColor)
                                            .font(.system(size: 20, weight: .regular))
                                }
                        )
                                .padding(.leading, 8)
                    }
                            .padding(.top, 11)
                }

                ZStack {

                    let checklistUI = state.checklistUI

                    VStack {

                        let isImportantTasksExists = !state.importantTasks.isEmpty

                        if let checklistUI = checklistUI {
                            VStack {
                                ChecklistView(checklistUI: checklistUI)
                                MainDivider()
                            }
                        }

                        if isImportantTasksExists {
                            let listHeight: CGFloat =
                                    checklistUI == nil ? .infinity :
                                    (taskListContentPadding * 2.0) +
                                    (taskItemHeight * state.importantTasks.count.toDouble().limitMax(5.1))
                            ImportantTasksView(
                                    tasks: state.importantTasks
                            )
                                    .frame(height: listHeight)
                        }

                        if !isImportantTasksExists && checklistUI == nil {
                            Spacer()
                        }
                    }
                            .padding(.bottom, navAndTasksTextHeight)

                    if (state.isTasksVisible) {

                        VStack {

                            if let checklistUI = checklistUI {

                                Button(
                                        action: {
                                            vm.toggleIsTasksVisible()
                                        },
                                        label: {
                                            Text(checklistUI.titleToExpand)
                                                    .foregroundColor(.white)
                                                    .frame(maxWidth: .infinity)
                                                    .padding(.top, 6)
                                                    .padding(.bottom, 12)
                                        }
                                )
                                        .background(.black)

                                MainDivider()
                            }

                            TasksView()
                                    .clipped() // Fix list offset on IME open
                        }
                                .padding(.bottom, bottomNavigationHeight)
                    }
                }
            }

            //
            // Navigation

            HStack(alignment: .bottom) {

                Button(
                        action: {
                            timetoSheet.showActivitiesTimerSheet(
                                    isPresented: $isTimerActivitiesPresented,
                                    timerContext: nil,
                                    withMenu: true,
                                    selectedActivity: nil,
                                    onStart: {
                                        isTimerActivitiesPresented = false
                                    }
                            )
                        },
                        label: {
                            VStack {
                                Spacer()
                                Image(systemName: "timer")
                                        .frame(height: menuIconSize)
                                        .foregroundColor(menuColor)
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

                                    Text(state.tasksText)
                                            .foregroundColor(menuColor)
                                            .font(.system(size: 15, weight: .regular))
                                            .padding(.top, 10)

                                    Spacer()
                                }

                                VStack(alignment: .center) {

                                    Text(state.timeOfTheDay)
                                            .foregroundColor(menuTimeColor)
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
                                            .background(
                                                    RoundedRectangle(cornerRadius: 99, style: .circular)
                                                            .fill(state.batteryBackground.toColor())
                                            )
                                }
                                        .padding(.top, 2)
                                        .frame(height: bottomNavigationHeight)
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
                                        .foregroundColor(menuColor)
                                        .font(.system(size: 30, weight: .thin))
                                        .frame(maxWidth: .infinity)
                            }
                        }
                )
            }
                    .frame(width: .infinity, height: state.isTasksVisible ? bottomNavigationHeight : navAndTasksTextHeight)
        }
                .ignoresSafeArea(.keyboard, edges: .bottom)
                .onReceive(shortcutPublisher) { shortcut in
                    let swiftURL = URL(string: shortcut.uri)!
                    if !UIApplication.shared.canOpenURL(swiftURL) {
                        UtilsKt.showUiAlert(message: "Invalid shortcut link", reportApiText: nil)
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
                        ChecklistDialog(isPresented: $isTriggersChecklistPresented, checklist: checklist)
                    }
                }
                .sheetEnv(
                        isPresented: $isSettingsSheetPresented
                ) {
                    SettingsSheet(isPresented: $isSettingsSheetPresented)
                }
                .onAppear {
                    MainView.lastInstance = self
                }
    }
}

private struct ChecklistView: View {

    let checklistUI: MainVM.ChecklistUI

    @State private var vScroll = 0

    var body: some View {

        let checkboxSize = 20.0
        let checklistItemMinHeight = 46.0

        VStack {

            MainDivider(isVisible: vScroll > 0)

            HStack(alignment: .top) {

                ScrollViewWithVListener(showsIndicators: false, vScroll: $vScroll) {

                    VStack {

                        ForEach(checklistUI.itemsUI, id: \.item.id) { itemUI in

                            Button(
                                    action: {
                                        itemUI.toggle()
                                    },
                                    label: {
                                        HStack {

                                            Image(systemName: itemUI.item.isChecked ? "checkmark.square.fill" : "square")
                                                    .foregroundColor(Color.white)
                                                    .font(.system(size: checkboxSize, weight: .regular))
                                                    .padding(.trailing, 12)

                                            Text(itemUI.item.text)
                                                    .padding(.vertical, 4)
                                                    .foregroundColor(.white)
                                                    .font(.system(size: 18))
                                        }
                                                .frame(maxWidth: .infinity, alignment: .leading)
                                                .frame(minHeight: checklistItemMinHeight)
                                        //                                                .background(Color.red)
                                    }
                            )
                        }
                    }

                    Spacer()
                }

                let stateUI = checklistUI.stateUI
                let stateIconResource: String = {
                    if stateUI is ChecklistStateUI.Completed { return "checkmark.square.fill" }
                    if stateUI is ChecklistStateUI.Empty { return "square" }
                    if stateUI is ChecklistStateUI.Partial { return "minus.square.fill" }
                    fatalError()
                }()
                Button(
                        action: {
                            stateUI.onClick()
                        },
                        label: {
                            Image(systemName: stateIconResource)
                                    .foregroundColor(Color.white)
                                    .font(.system(size: checkboxSize, weight: .regular))
                        }
                )
                        .frame(height: checklistItemMinHeight)
            }
                    .padding(.horizontal, TAB_TASKS_H_PADDING)
        }
    }
}

private struct ImportantTasksView: View {

    let tasks: [MainVM.ImportantTask]

    private let LIST_BOTTOM_ITEM_ID = "bottom_id"

    var body: some View {

        GeometryReader { geometry in

            ScrollViewReader { scrollProxy in

                ScrollView(showsIndicators: false) {

                    VStack {

                        Spacer()

                        ZStack {}
                                .frame(height: taskListContentPadding)

                        ForEach(tasks.reversed(), id: \.self.task.id) { importantTask in
                            ImportantTaskItem(importantTask: importantTask)
                        }

                        ZStack {}
                                .frame(height: taskListContentPadding)
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

private struct ImportantTaskItem: View {

    let importantTask: MainVM.ImportantTask

    @State private var isSheetPresented = false

    @EnvironmentObject private var timetoSheet: TimetoSheet

    var body: some View {

        Button(
                action: {
                    importantTask.task.startIntervalForUI(
                            onStarted: {},
                            activitiesSheet: {
                                timetoSheet.showActivitiesTimerSheet(
                                        isPresented: $isSheetPresented,
                                        timerContext: importantTask.timerContext,
                                        withMenu: false,
                                        selectedActivity: nil,
                                        onStart: {
                                            isSheetPresented = false
                                        }
                                )
                            },
                            timerSheet: { activity in
                                timetoSheet.showActivitiesTimerSheet(
                                        isPresented: $isSheetPresented,
                                        timerContext: importantTask.timerContext,
                                        withMenu: false,
                                        selectedActivity: activity,
                                        onStart: {
                                            isSheetPresented = false
                                        }
                                )
                            }
                    )
                },
                label: {

                    let type = importantTask.type

                    HStack {

                        HStack {

                            if (type != nil) {
                                var (iconRes, iconSize): (String, CGFloat) = {
                                    if (importantTask.type == .event) {
                                        return ("calendar", 14)
                                    } else if (importantTask.type == .repeating) {
                                        return ("repeat", 14)
                                    } else if (importantTask.type == .paused) {
                                        return ("pause.fill", 13)
                                    }
                                    return ("", 14)
                                }()
                                Image(systemName: iconRes)
                                        .foregroundColor(Color.white)
                                        .font(.system(size: iconSize, weight: .light))
                                        .padding(.trailing, 3)
                            }

                            Text(importantTask.text)
                                    .font(.system(size: 15))
                                    .foregroundColor(Color.white)
                        }
                                .padding(.horizontal, 8)
                                .frame(maxHeight: .infinity)
                                .background(
                                        RoundedRectangle(cornerRadius: 99, style: .circular)
                                                .fill(importantTask.backgroundColor.toColor())
                                )
                                .padding(.all, 1)
                                .background(
                                        RoundedRectangle(cornerRadius: 99, style: .circular)
                                                .fill(importantTask.borderColor.toColor())
                                )
                                .padding(.vertical, 4)
                                .padding(.horizontal, TAB_TASKS_H_PADDING)
                    }
                            .frame(height: taskItemHeight)
                }
        )
    }
}

private struct MainDivider: View {

    var isVisible = true

    var body: some View {
        DividerBg(isVisible: isVisible)
                .padding(.horizontal, TAB_TASKS_H_PADDING)
    }
}
