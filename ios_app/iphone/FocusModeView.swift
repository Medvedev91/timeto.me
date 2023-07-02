import SwiftUI
import Combine
import shared

private let bottomNavigationHeight = 56.0 // todo

private let dividerPadding = 8.0
private let dividerColor: UIColor = .systemGray4

private let menuIconSize = bottomNavigationHeight

private let taskCountsHeight = 36.0

private let taskItemHeight = 36.0
private let taskListContentPadding = 4.0

private let menuColor = FocusModeVM.companion.menuColor.toColor()

extension View {

    func attachFocusModeView() -> some View {
        modifier(FocusModeView__ViewModifier())
    }
}

///
///

private struct FocusModeView__ViewModifier: ViewModifier {

    @State private var isPresented = false

    private let statePublisher: AnyPublisher<KotlinBoolean, Never> = FocusModeUI.shared.state.toPublisher()

    func body(content: Content) -> some View {

        ZStack {
            content
            if isPresented {
                FocusModeView__CoverView()
                        .colorScheme(.dark)
                        .ignoresSafeArea(.keyboard, edges: .bottom)
            }
        }
                .onReceive(statePublisher) { newValue in
                    isPresented = newValue.boolValue
                }
    }
}

private struct FocusModeView__CoverView: View {

    @State private var vm = FocusModeVM()
    @State private var isTimerActivitiesPresented = false

    @EnvironmentObject private var timetoSheet: TimetoSheet

    @State private var isPurpleAnim = true

    var body: some View {

        VMView(vm: vm, stack: .ZStack(alignment: .bottom)) { state in

            let navAndTasksTextHeight = bottomNavigationHeight + taskCountsHeight

            Color.black.edgesIgnoringSafeArea(.all)
                    .statusBar(hidden: true)
                    .animateVmValue(value: state.isPurple, state: $isPurpleAnim)

            VStack {

                HStack(alignment: .center) {

                    if isPurpleAnim {
                        Text(state.activity.emoji)
                                .transition(.move(edge: .trailing).combined(with: .opacity))
                                .font(.system(size: 20))
                                .padding(.trailing, 6)
                    }

                    Button(
                            action: {
                                vm.toggleIsPurple()
                            },
                            label: {
                                Text(state.title)
                                        .font(.system(size: 20))
                                        .foregroundColor(.white)
                                        .multilineTextAlignment(.center)
                            }
                    )

                    if isPurpleAnim {

                        Button(
                                action: {
                                    vm.pauseTask()
                                },
                                label: {
                                    Image(systemName: "pause.fill")
                                            .foregroundColor(.blue)
                                            .font(.system(size: 14))
                                }
                        )
                                .frame(width: 24, height: 24)
                                .background(roundedShape.fill(.white))
                                .padding(.leading, 10)
                                .transition(.move(edge: .leading).combined(with: .opacity))
                                .offset(y: onePx)
                    }
                }
                        .padding(.horizontal, 20)

                let timerData = state.timerData

                if let subtitle = timerData.subtitle, !state.isTabTasksVisible {
                    Button(
                            action: {
                                vm.toggleIsPurple()
                            },
                            label: {
                                Text(subtitle)
                                        .font(.system(size: 26, weight: .heavy))
                                        .tracking(5)
                                        .foregroundColor(timerData.color.toColor())
                                        .padding(.top, 36)
                                        .offset(y: 3)
                            }
                    )
                }

                Button(
                        action: {
                            vm.toggleIsPurple()
                        },
                        label: {
                            Text(timerData.title)
                                    .font(.system(size: 70, design: .monospaced))
                                    .fontWeight(.bold)
                                    .foregroundColor(timerData.color.toColor())
                                    .opacity(0.9)
                        }
                )

                if timerData.subtitle != nil || state.isPurple {
                    Button(
                            action: {
                                vm.restart()
                            },
                            label: {
                                Text("Restart")
                                        .font(.system(size: 24, weight: .regular))
                                        .foregroundColor(.white)
                            }
                    )
                }

                ZStack {

                    let checklistUI = state.checklistUI

                    VStack {

                        let isImportantTasksExists = !state.importantTasks.isEmpty

                        if let checklistUI = checklistUI {
                            VStack(spacing: 0) {
                                ChecklistView(checklistUI: checklistUI)
                                FSDivider()
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

                    if (state.isTabTasksVisible) {
                        VStack {
                            if let checklistUI = checklistUI {
                                Button(
                                        action: {
                                            vm.toggleIsTabTasksVisible()
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
                                MyDivider()
                            }

                            TabTasksView(
                                    withRepeatings: false,
                                    onTaskStarted: {}
                            )
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
                                    selectedActivity: nil,
                                    onStart: {
                                        isTimerActivitiesPresented = false
                                    }
                            )
                        },
                        label: {
                            VStack(spacing: 0) {
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
                            vm.toggleIsTabTasksVisible()
                        },
                        label: {

                            VStack {

                                if (!state.isTabTasksVisible) {

                                    Text(state.tasksText)
                                            .foregroundColor(menuColor)
                                            .font(.system(size: 15, weight: .regular))
                                            .padding(.top, 10)

                                    Spacer()
                                }

                                VStack(alignment: .center) {

                                    Text(state.timeOfTheDay)
                                            .foregroundColor(menuColor)
                                            .font(.system(size: 17, weight: .bold))

                                    HStack(spacing: 0) {

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
                                    .background(state.isTabTasksVisible ? Color(.systemGray5) : .black)
                                    .cornerRadius(10, onTop: true, onBottom: true)
                        }
                )

                Button(
                        action: {
                            FocusModeUI.shared.close()
                        },
                        label: {
                            VStack(spacing: 0) {
                                Spacer()
                                Image(systemName: "xmark.circle")
                                        .frame(height: menuIconSize)
                                        .foregroundColor(menuColor)
                                        .font(.system(size: 30, weight: .thin))
                                        .frame(maxWidth: .infinity)
                            }
                        }
                )
            }
                    .frame(width: .infinity, height: state.isTabTasksVisible ? bottomNavigationHeight : navAndTasksTextHeight)
        }
                .onAppear {
                    UIApplication.shared.isIdleTimerDisabled = true
                }
                .onDisappear {
                    UIApplication.shared.isIdleTimerDisabled = false
                }
    }
}

private struct ChecklistView: View {

    let checklistUI: FocusModeVM.ChecklistUI

    @State private var vScroll = 0

    var body: some View {

        VStack(spacing: 0) {

            FSDivider(isVisible: vScroll > 0)

            GeometryReader { proxy in

                ZStack(alignment: .center) {

                    HStack(alignment: .top, spacing: 0) {

                        let checkboxSize = 20.0
                        let checklistItemMinHeight = 46.0
                        let checklistDividerPadding = 12.0

                        ScrollViewWithVListener(showsIndicators: false, vScroll: $vScroll) {

                            VStack(spacing: 0) {

                                ForEach(checklistUI.itemsUI, id: \.item.id) { itemUI in

                                    Button(
                                            action: {
                                                itemUI.toggle()
                                            },
                                            label: {
                                                HStack(spacing: 0) {

                                                    Image(systemName: itemUI.item.isChecked ? "checkmark.square.fill" : "square")
                                                            .foregroundColor(Color.white)
                                                            .font(.system(size: checkboxSize, weight: .regular))
                                                            .padding(.trailing, checklistDividerPadding)

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

                        let dividerGap = 8.0
                        Color(dividerColor)
                                .frame(width: onePx)
                                .frame(height: checklistItemMinHeight - dividerGap)
                                .padding(.top, dividerGap / 2)
                                .padding(.trailing, checklistDividerPadding)

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
                                            .padding(.trailing, checklistDividerPadding)
                                }
                        )
                                .frame(height: checklistItemMinHeight)
                    }
                            .frame(width: proxy.size.width * 0.80)
                }
                        .frame(maxWidth: .infinity)
            }
        }
                .padding(.top, 20)
    }
}

private struct ImportantTasksView: View {

    let tasks: [FocusModeVM.ImportantTask]

    private let LIST_BOTTOM_ITEM_ID = "bottom_id"

    var body: some View {

        GeometryReader { geometry in

            ScrollViewReader { scrollProxy in

                ScrollView(showsIndicators: false) {

                    VStack(spacing: 0) {

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

    let importantTask: FocusModeVM.ImportantTask

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
                                        selectedActivity: activity,
                                        onStart: {
                                            isSheetPresented = false
                                        }
                                )
                            }
                    )
                },
                label: {
                    HStack(spacing: 0) {

                        HStack(spacing: 0) {

                            Image(systemName: "calendar")
                                    .foregroundColor(Color.white)
                                    .font(.system(size: 15, weight: .light))
                                    .padding(.trailing, 3)

                            Text(importantTask.text)
                                    .font(.system(size: 15))
                                    .foregroundColor(Color.white)
                        }
                                .padding(.horizontal, 6)
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
                                .padding(.horizontal, dividerPadding)
                    }
                            .frame(height: taskItemHeight)
                }
        )
    }
}

private struct FSDivider: View {

    var isVisible = true

    var body: some View {
        Color(isVisible ? dividerColor : .clear)
                .frame(height: onePx)
                .padding(.horizontal, dividerPadding)
    }
}
