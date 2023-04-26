import SwiftUI
import Combine
import shared

private let dividerPadding = 8.0
private let dividerColor = UIColor.systemGray4
private let dividerHeight = 1 / UIScreen.main.scale

private let taskItemHeight = 36.0
private let taskListContentPadding = 4.0

extension View {

    func attachTimerFullScreenView() -> some View {
        modifier(TimerFullScreen__ViewModifier())
    }
}

///
///

private struct TimerFullScreen__ViewModifier: ViewModifier {

    @State private var isPresented = false

    private let statePublisher: AnyPublisher<KotlinBoolean, Never> = FullScreenUI.shared.state.toPublisher()

    func body(content: Content) -> some View {

        content
                /// Скрывание status bar в .statusBar(...)
                .fullScreenCover(isPresented: $isPresented) {
                    TimerFullScreen__FullScreenCoverView()
                }
                .onReceive(statePublisher) { newValue in
                    isPresented = newValue.boolValue
                }
    }
}

private struct TimerFullScreen__FullScreenCoverView: View {

    @State private var vm = FullScreenVM()
    @State private var isTimerActivitiesPresented = false
    @State private var isTasksSheetPresented = false

    var body: some View {

        VMView(vm: vm, stack: .ZStack()) { state in

            Color.black.edgesIgnoringSafeArea(.all)
                    .statusBar(hidden: true)

            VStack(spacing: 0) {

                VStack(spacing: 0) {

                    Button(
                            action: {
                                vm.toggleIsTaskCancelVisible()
                            },
                            label: {
                                Text(state.title)
                                        .font(.system(size: 20))
                                        .foregroundColor(.white)
                            }
                    )

                    if (state.isTaskCancelVisible) {

                        Button(
                                action: {
                                    vm.cancelTask()
                                },
                                label: {
                                    Text(state.cancelTaskText)
                                            .padding(.vertical, 4)
                                            .padding(.horizontal, 8)
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(.white)
                                            .background(
                                                    RoundedRectangle(cornerRadius: 99, style: .circular)
                                                            .fill(.blue)
                                            )
                                            .padding(.vertical, 12)
                                }
                        )
                    }
                }

                let timerData = state.timerData

                if let subtitle = timerData.subtitle {
                    Text(subtitle)
                            .font(.system(size: 26, weight: .heavy))
                            .tracking(5)
                            .foregroundColor(timerData.subtitleColor.toColor())
                            .padding(.top, 36)
                            .offset(y: 3)
                }

                Button(
                        action: {
                            vm.toggleIsCountdown()
                        },
                        label: {
                            Text(timerData.title)
                                    .font(.system(size: 70, design: .monospaced))
                                    .fontWeight(.bold)
                                    .foregroundColor(timerData.titleColor.toColor())
                                    .opacity(0.9)
                        }
                )

                if timerData.subtitle != nil || !state.isCountdown {
                    Button(
                            action: {
                                vm.restart()
                            },
                            label: {
                                Text("Restart")
                                        .font(.system(size: 25, weight: .light))
                                        .foregroundColor(.white)
                                        .tracking(1)
                            }
                    )
                            .padding(.top, 10)
                }

                ZStack(alignment: .bottom) {

                    let checklistUI = state.checklistUI

                    VStack(spacing: 0) {

                        if let checklistUI = checklistUI {
                            ChecklistView(checklistUI: checklistUI)
                        } else {
                            Spacer(minLength: 0)
                        }

                        let listHeight = taskItemHeight * state.importantTasks.count.toDouble()
                                         + dividerHeight
                                         + taskListContentPadding * 2.0
                        ImportantTasksView(
                                isNavDividerVisible: checklistUI != nil,
                                tasks: state.importantTasks
                        )
                                .frame(height: listHeight)
                    }
                }
                        .frame(maxHeight: .infinity)

                HStack(alignment: .bottom, spacing: 0) {

                    let menuColor = state.menuColor.toColor()

                    Button(
                            action: {
                                isTimerActivitiesPresented = true
                            },
                            label: {
                                VStack(spacing: 0) {
                                    Spacer(minLength: 0)
                                    Image(systemName: "timer")
                                            .padding(.bottom, 4)
                                            .foregroundColor(menuColor)
                                            .font(.system(size: 30, weight: .thin))
                                            .frame(maxWidth: .infinity)
                                            .frame(alignment: .bottom)
                                }
                            }
                    )
                            .sheetEnv(isPresented: $isTimerActivitiesPresented) {
                                ActivitiesTimerSheet(
                                        isPresented: $isTimerActivitiesPresented,
                                        timerContext: nil
                                ) {
                                    isTimerActivitiesPresented = false
                                }
                            }

                    Button(
                            action: {
                                isTasksSheetPresented = true
                            },
                            label: {

                                VStack(spacing: 0) {

                                    Text(state.tasksText)
                                            .foregroundColor(menuColor)
                                            .font(.system(size: 14, weight: .regular))
                                            .padding(.top, 2)

                                    Spacer(minLength: 0)

                                    Text(state.timeOfTheDay)
                                            .padding(.horizontal, 16)
                                            .foregroundColor(menuColor)
                                            .padding(.top, 4)
                                            .font(.system(size: 16, weight: .bold))

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
                            }
                    )

                    Button(
                            action: {
                                FullScreenUI.shared.close()
                            },
                            label: {
                                VStack(spacing: 0) {
                                    Spacer(minLength: 0)
                                    Image(systemName: "xmark.circle")
                                            .padding(.bottom, 4)
                                            .foregroundColor(menuColor)
                                            .font(.system(size: 30, weight: .thin))
                                            .frame(maxWidth: .infinity)
                                }
                            }
                    )
                }
                        .frame(height: 80)
                        .frame(width: .infinity)
            }
                    .sheetEnv(isPresented: $isTasksSheetPresented) {
                        TasksSheet(isPresented: $isTasksSheetPresented)
                                .ignoresSafeArea(.keyboard, edges: .bottom)
                                .colorScheme(.dark)
                    }
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

    let checklistUI: FullScreenVM.ChecklistUI

    @State private var vScroll = 0

    var body: some View {

        VStack(spacing: 0) {

            Color(vScroll > 0 ? dividerColor : .clear)
                    .frame(height: dividerHeight)
                    .padding(.horizontal, dividerPadding)

            HStack(alignment: .top, spacing: 0) {

                let checkboxSize = 20.0
                let checklistItemMinHeight = 44.0
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
                        .frame(width: dividerHeight)
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
                    .padding(.horizontal, 50)
        }
                .padding(.top, 20)
    }
}

private struct ImportantTasksView: View {

    let isNavDividerVisible: Bool
    let tasks: [FullScreenVM.ImportantTask]

    private let LIST_BOTTOM_ITEM_ID = "bottom_id"

    var body: some View {

        GeometryReader { geometry in

            ScrollViewReader { scrollProxy in

                VStack(spacing: 0) {

                    Color(isNavDividerVisible ? dividerColor : .clear)
                            .frame(height: dividerHeight)
                            .padding(.horizontal, dividerPadding)

                    ScrollView(showsIndicators: false) {

                        VStack(spacing: 0) {

                            Spacer(minLength: 0)

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
}

private struct ImportantTaskItem: View {

    let importantTask: FullScreenVM.ImportantTask

    @State private var isSheetPresented = false

    var body: some View {

        Button(
                action: {
                    importantTask.task.startIntervalForUI(
                            onStarted: {},
                            needSheet: {
                                isSheetPresented = true
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
                    }
                            .frame(height: taskItemHeight)
                }
        )
                .sheetEnv(isPresented: $isSheetPresented) {
                    ActivitiesTimerSheet(
                            isPresented: $isSheetPresented,
                            timerContext: importantTask.timerContext
                    ) {
                        isSheetPresented = false
                    }
                }
    }
}

private struct TasksSheet: View {

    @Binding var isPresented: Bool

    var body: some View {

        VStack(spacing: 0) {

            TabTasksView()

            VStack(spacing: 0) {

                MyDivider()

                Button(
                        action: {
                            isPresented = false
                        },
                        label: {
                            Text("focus")
                                    .foregroundColor(.white)
                        }
                )
                        .padding(.top, 20)

                Image(systemName: "chevron.compact.down")
                        .padding(.top, 4)
                        .padding(.bottom, 4)
                        .foregroundColor(.white)
                        .font(.system(size: 30, weight: .thin))
            }
        }
                .background(Color(.myBackground))
    }
}
