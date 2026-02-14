import SwiftUI
import shared

struct WatchTabTasksView: View {
    
    var body: some View {
        VmView({
            WatchTabTasksVm()
        }) { _, state in
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
                ForEach(tasksUI, id: \.taskDb.id) { taskUI in
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
                                    WatchTabsView.lastInstance?.tabSelection = WatchTabsView.TAB_ID_TIMER
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
                                    .foregroundColor(timeUI.textColorEnum.toColor())
                                    .lineLimit(1)
                            }
                            
                            Text(taskUI.listText)
                        }
                    }
                )
                .sheet(isPresented: $isActivitiesPresented) {
                    TaskSheetDialog(taskDb: taskUI.taskDb, isPresented: $isActivitiesPresented)
                }
            }
            
            private struct TaskSheetDialog: View {
                
                let taskDb: TaskDb
                @Binding var isPresented: Bool
                
                var body: some View {
                    VmView({
                        WatchTaskSheetVm(taskDb: taskDb)
                    }) { _, state in
                        List {
                            ForEach(state.goalsUi, id: \.goalDb.id) { goalUi in
                                ActivityView(
                                    taskSheetDialog: self,
                                    goalUi: goalUi,
                                    task: taskDb,
                                )
                            }
                        }
                    }
                }
                
                private struct ActivityView: View {
                    
                    let taskSheetDialog: TaskSheetDialog
                    let goalUi: WatchTaskSheetVm.GoalUi
                    var task: TaskDb
                    
                    var body: some View {
                        Button(
                            action: {
                                startDefaultTimer()
                                goHome()
                            },
                            label: {
                                VStack {
                                    
                                    Text(goalUi.listTitle)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                        .lineLimit(1)
                                        .truncationMode(.middle)
                                    
                                    if !goalUi.timerHintsUi.isEmpty {
                                        HStack(spacing: 6) {
                                            ForEach(goalUi.timerHintsUi, id: \.seconds) { hintUi in
                                                Button(
                                                    action: {
                                                        hintUi.startInterval()
                                                        goHome()
                                                    },
                                                    label: {
                                                        Text(hintUi.text)
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
                                                    startDefaultTimer()
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
                    }
                    
                    private func startDefaultTimer() {
                        goalUi.onTap()
                    }
                    
                    private func goHome() {
                        taskSheetDialog.isPresented = false
                        myAsyncAfter(0.05) { // Меньше 0.2 на железе не работает
                            WatchTabsView.lastInstance?.tabSelection = WatchTabsView.TAB_ID_TIMER
                        }
                    }
                }
            }
        }
    }
}
