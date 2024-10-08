import SwiftUI
import shared

struct W_TabTasksView: View {

    @State private var vm = WatchTabTasksVm()

    var body: some View {
        VMView(vm: vm) { state in
            List {
                ForEach(state.foldersUI, id: \.title) { folderUI in
                    FolderView(title: folderUI.title, tasksUI: folderUI.tasks)
                }
            }
        }
    }

    private struct FolderView: View {

        let title: String
        let tasksUI: [WatchTabTasksVm.TaskUI]

        var body: some View {

            Section(title) {
                ForEach(tasksUI, id: \.task.id) { taskUI in
                    TaskView(taskUI: taskUI)
                }
            }
        }

        private struct TaskView: View {

            var taskUI: WatchTabTasksVm.TaskUI
            @State private var isActivitiesPresented = false

            var body: some View {
                Button(
                        action: {
                            taskUI.start(
                                    onStarted: {
                                        withAnimation {
                                            W_TabsView.lastInstance?.tabSelection = W_TabsView.TAB_ID_TIMER
                                        }
                                    },
                                    needSheet: {
                                        isActivitiesPresented = true
                                    }
                            )
                        },
                        label: {
                            VStack(alignment: .leading) {

                                if let timeUI = taskUI.timeUI {
                                    Text(timeUI.text)
                                            .padding(.top, 1)
                                            .padding(.bottom, 2)
                                            .font(.system(size: 14, weight: .light))
                                            .foregroundColor(timeUI.textColor.toColor())
                                            .lineLimit(1)
                                }

                                Text(taskUI.listText)
                            }
                        }
                )
                        .sheet(isPresented: $isActivitiesPresented) {
                            TaskSheetDialog(task: taskUI.task, isPresented: $isActivitiesPresented)
                        }
            }

            private struct TaskSheetDialog: View {

                @State private var vm: WatchTaskSheetVm

                let task: TaskDb
                @Binding var isPresented: Bool

                init(task: TaskDb, isPresented: Binding<Bool>) {
                    self.task = task
                    _isPresented = isPresented
                    _vm = State(initialValue: WatchTaskSheetVm(task: task))
                }

                var body: some View {
                    VMView(vm: vm) { state in
                        List {
                            ForEach(state.activitiesUI, id: \.activity.id) { activityUI in
                                ActivityView(
                                        taskSheetDialog: self,
                                        activityUI: activityUI,
                                        task: task
                                )
                            }
                        }
                    }
                }

                private struct ActivityView: View {

                    let taskSheetDialog: TaskSheetDialog
                    let activityUI: WatchTaskSheetVm.ActivityUI
                    var task: TaskDb
                    @State private var isTickerPresented = false

                    var body: some View {
                        Button(
                                action: {
                                    isTickerPresented = true
                                },
                                label: {
                                    VStack {

                                        Text(activityUI.listTitle)
                                                .frame(maxWidth: .infinity, alignment: .leading)
                                                .lineLimit(1)
                                                .truncationMode(.middle)

                                        if !activityUI.timerHints.isEmpty {
                                            HStack(spacing: 6) {
                                                ForEach(activityUI.timerHints, id: \.seconds) { hintUI in
                                                    Button(
                                                            action: {
                                                                hintUI.startInterval {}
                                                                taskSheetDialog.isPresented = false
                                                                myAsyncAfter(0.05) { // Меньше 0.2 на железе не работает
                                                                    W_TabsView.lastInstance?.tabSelection = W_TabsView.TAB_ID_TIMER
                                                                }
                                                            },
                                                            label: {
                                                                Text(hintUI.text)
                                                                        .font(.system(size: 13, weight: .medium))
                                                                        .foregroundColor(.white)
                                                                        .cornerRadius(99)
                                                                        .lineLimit(1)
                                                            }
                                                    )
                                                            .buttonStyle(.borderless)
                                                }
                                                // Без этой кнопки при нажатии по пустому месте рядом с последней
                                                // подсказкой срабатывает подсказка, хотя нужно открыать таймер.
                                                Button(
                                                        action: {
                                                            isTickerPresented = true
                                                        },
                                                        label: {
                                                            Text(" ")
                                                        }
                                                )
                                                        .buttonStyle(.borderless)
                                                Spacer()
                                            }
                                        }
                                    }
                                }
                        )
                                .sheet(isPresented: $isTickerPresented) {
                                    /// The logic of adding a task from the ticker sheet:
                                    /// - As soon as the user pressed "Start", i.e. before the actual addition:
                                    ///    - To make the UI responsive, we immediately start a closing
                                    ///      animation of the timer dialog;
                                    ///    - Without the animation, change the tab to the timer, this is not
                                    ///      noticeable because the activity dialog is still open above it.
                                    /// - After adding an interval, we start closing the dialog of activities,
                                    ///   and immediately get the ticker screen with the selected activity on top.
                                    ///   The time of addition should be enough to avoid simultaneous closing of
                                    ///   the dialogs. But then there is an additional insurance. A small delay
                                    ///   so as not to see twitching of the timer window refresh and an additional
                                    ///   insurance against simultaneous closing of several dialogs.
                                    W_TickerDialog(
                                            activity: activityUI.activity,
                                            task: task,
                                            preAdd: {
                                                W_TabsView.lastInstance?.tabSelection = W_TabsView.TAB_ID_TIMER
                                                isTickerPresented = false
                                                myAsyncAfter(0.05) { /// Иначе не закрывается
                                                    taskSheetDialog.isPresented = false
                                                }
                                            }
                                    )
                                }
                    }
                }
            }
        }
    }
}
