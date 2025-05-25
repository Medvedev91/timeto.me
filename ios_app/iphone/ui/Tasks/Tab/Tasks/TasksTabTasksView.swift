import SwiftUI
import shared

struct TasksTabTasksView: View {
    
    let taskFolderDb: TaskFolderDb
    let tasksTabView: TasksTabViewInner
    
    var body: some View {
        VmView({
            TasksTabTasksVm(
                taskFolderDb: taskFolderDb
            )
        }) { vm, state in
            TasksTabTasksViewInner(
                vm: vm,
                state: state,
                tasksTabView: tasksTabView
            )
        }
        .id("TasksTabTasksView_\(taskFolderDb.id)")
    }
}

private struct TasksTabTasksViewInner: View {
    
    let vm: TasksTabTasksVm
    let state: TasksTabTasksVm.State
    
    let tasksTabView: TasksTabViewInner
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        ScrollView(.vertical, showsIndicators: false) {
            
            VStack {
                
                if let tmrwUi = state.tmrwUi {
                    let tmrwTasksUi = tmrwUi.tasksUi.reversed()
                    ForEach(tmrwTasksUi, id: \.taskDb.id) { taskUi in
                        let isFirst = tmrwTasksUi.first == taskUi
                        ZStack(alignment: .top) {
                            TmrwTaskView(taskUi: taskUi)
                                .id("tmrw \(taskUi.taskDb.id)")
                            if !isFirst {
                                Divider()
                                    .padding(.leading, H_PADDING)
                            }
                        }
                    }
                    
                    Divider()
                        .padding(.horizontal, 80)
                        .padding(.top, 20)
                        .padding(.bottom, state.tasksVmUi.isEmpty ? 0 : 20)
                }
                
                let tasksUiReversed = state.tasksVmUi.reversed()
                VStack {
                    ForEach(tasksUiReversed, id: \.taskUi.taskDb.id) { taskVmUi in
                        TaskRowView(
                            taskVmUi: taskVmUi,
                            tasksTabView: tasksTabView,
                            withDivider: tasksUiReversed.last != taskVmUi
                        )
                        .id(taskVmUi.taskUi.taskDb.id)
                    }
                }
                
                if let tmrwUi = state.tmrwUi {
                    HStack {
                        Spacer()
                        Text(tmrwUi.curTimeString)
                            .font(.system(size: 14, weight: .light))
                        
                        Spacer()
                    }
                    .padding(.top, 24)
                }
                
                HStack {
                    
                    HStack {
                        
                        Text("Task")
                            .foregroundColor(.secondary)
                            .padding(.leading, 12)
                        
                        Spacer()
                    }
                    .frame(minHeight: 44)
                    
                    Text("SAVE")
                        .padding(.horizontal, 12)
                        .font(.system(size: 14, weight: .bold))
                        .frame(height: 34)
                        .foregroundColor(.white)
                        .background(
                            RoundedRectangle(cornerRadius: 8, style: .continuous)
                                .fill(.blue)
                        )
                        .padding(.trailing, 6)
                }
                // trick x2 overlay looks better
                .overlay(squircleShape.stroke(Color(.systemGray4), lineWidth: onePx))
                .overlay(squircleShape.stroke(Color(.systemGray4), lineWidth: onePx))
                .padding(.leading, H_PADDING - 2)
                .padding(.top, 20)
                .padding(.bottom, 20)
                .background(.black)
                .onTapGesture {
                    navigation.showTaskForm(
                        strategy: TaskFormStrategy.NewTask(
                            taskFolderDb: vm.taskFolderDb
                        )
                    )
                }
            }
        }
        .padding(.trailing, H_PADDING)
        .defaultScrollAnchor(.bottom)
    }
}

private let taskRowButtonStyle = MyButtonStyle()

private struct TaskRowView: View {
    
    @Environment(Navigation.self) private var navigation
    
    private let taskVmUi: TasksTabTasksVm.TaskVmUi
    
    let tasksTabView: TasksTabViewInner
    @State private var dragItem: TasksTabDragItem
    
    @State private var xSwipeOffset: CGFloat = 0
    @State private var width: CGFloat? = nil
    
    @State private var itemHeight: CGFloat = 0
    
    private let withDivider: Bool
    
    init(
        taskVmUi: TasksTabTasksVm.TaskVmUi,
        tasksTabView: TasksTabViewInner,
        withDivider: Bool
    ) {
        self.taskVmUi = taskVmUi
        self.tasksTabView = tasksTabView
        self.withDivider = withDivider
        
        _dragItem = State(initialValue: TasksTabDragItem(
            isDropAllowed: { drop in
                if drop is TasksTabDropItemCalendar {
                    return true
                }
                if let dropFolder = drop as? TasksTabDropItemTaskFolder {
                    return dropFolder.taskFolderDb.id != taskVmUi.taskUi.taskDb.folder_id
                }
                fatalError("Unknown tasks list drop type")
            }
        ))
    }
    
    var body: some View {
        
        ZStack(alignment: .bottom) {
            
            GeometryReader { proxy in
                ZStack {
                }
                .onAppear {
                    width = proxy.size.width
                }
                .onChange(of: proxy.frame(in: .global).minY) {
                    xSwipeOffset = 0
                    _ = tasksTabView.onDragStop()
                }
            }
            .frame(height: 1) // height: 1, or full height items if short
            
            if (xSwipeOffset > 0) {
                let editOrMoveTitle = tasksTabView.focusedDrop != nil ? "Move to \(tasksTabView.focusedDrop!.name)" : "Edit"
                HStack {
                    Text(editOrMoveTitle)
                        .foregroundColor(.white)
                        .padding(.leading, 16)
                    Spacer()
                }
                .frame(maxHeight: itemHeight)
                .background(tasksTabView.focusedDrop == nil ? .blue : .green)
                .offset(x: xSwipeOffset > 0 ? 0 : xSwipeOffset)
            }
            
            if (xSwipeOffset < 0) {
                
                HStack {
                    
                    Text(taskVmUi.text)
                        .padding(.leading, 12)
                        .padding(.trailing, 4)
                        .foregroundColor(.white)
                        .lineLimit(1)
                        .font(.system(size: 13, weight: .light))
                    
                    Spacer()
                    
                    Button("Cancel") {
                        xSwipeOffset = 0
                    }
                    .foregroundColor(.white)
                    .padding(.trailing, 12)
                    
                    Button(
                        action: {
                            taskVmUi.delete()
                        },
                        label: {
                            Text("Delete")
                                .fontWeight(.bold)
                                .padding(.horizontal, 9)
                                .padding(.vertical, 5)
                                .foregroundColor(.red)
                        }
                    )
                    .background(
                        RoundedRectangle(cornerRadius: 8, style: .continuous)
                            .fill(.white)
                    )
                    .padding(.trailing, 12)
                }
                .frame(maxHeight: itemHeight)
                .background(.red)
                .offset(x: xSwipeOffset < 0 ? 0 : xSwipeOffset)
            }
            
            ///
            
            ZStack {
                
                Button(
                    action: {
                        hideKeyboard()
                        taskVmUi.taskUi.taskDb.startIntervalForUi(
                            ifJustStarted: {},
                            ifActivityNeeded: {
                                navigation.showActivitiesTimerSheet(
                                    strategy: taskVmUi.timerStrategy
                                )
                            },
                            ifTimerNeeded: { activityDb in
                                navigation.showActivityTimerSheet(
                                    activityDb: activityDb,
                                    strategy: taskVmUi.timerStrategy,
                                    hideOnStart: true
                                )
                            }
                        )
                    },
                    label: {
                        
                        VStack {
                            
                            if let timeUI = taskVmUi.timeUi as? TasksTabTasksVm.TaskVmUiTimeUiHighlightUi {
                                
                                HStack {
                                    
                                    HStack {
                                        
                                        switch timeUI.timeData.type {
                                        case .event:
                                            Image(systemName: "calendar")
                                                .foregroundColor(.white)
                                                .font(.system(size: 16))
                                                .padding(.trailing, 3)
                                        case .repeating:
                                            Image(systemName: "repeat")
                                                .foregroundColor(.white)
                                                .font(.system(size: 13, weight: .medium))
                                                .padding(.trailing, 3)
                                        default:
                                            fatalError()
                                        }
                                        
                                        Text(timeUI.title)
                                            .foregroundColor(.white)
                                            .font(.system(size: 13))
                                    }
                                    .padding(.leading, 4)
                                    .padding(.trailing, 5)
                                    .padding(.top, 4)
                                    .padding(.bottom, 4)
                                    .background(
                                        RoundedRectangle(cornerRadius: 8, style: .continuous)
                                            .fill(timeUI.backgroundColor.toColor())
                                    )
                                    
                                    Text(timeUI.timeLeftText)
                                        .foregroundColor(timeUI.timeLeftColor.toColor())
                                        .font(.system(size: 14, weight: .light))
                                        .padding(.leading, 8)
                                        .lineLimit(1)
                                    
                                    Spacer()
                                }
                                .padding(.top, 2)
                                .padding(.bottom, 6)
                                .padding(.leading, H_PADDING - 1)
                                
                            } else if let timeUI = taskVmUi.timeUi as? TasksTabTasksVm.TaskVmUiTimeUiRegularUi {
                                HStack {
                                    Text(timeUI.text)
                                        .padding(.leading, H_PADDING)
                                        .padding(.top, 1)
                                        .padding(.bottom, 6)
                                        .font(.system(size: 14, weight: .light))
                                        .foregroundColor(timeUI.textColor.toColor())
                                        .lineLimit(1)
                                    Spacer()
                                }
                            }
                            
                            HStack {
                                
                                Text(taskVmUi.text)
                                    .lineSpacing(4)
                                    .textAlign(.leading)
                                
                                Spacer()
                                
                                TriggersIconsView(
                                    checklistsDb: taskVmUi.taskUi.tf.checklists,
                                    shortcutsDb: taskVmUi.taskUi.tf.shortcuts
                                )
                                
                                if (taskVmUi.taskUi.tf.isImportant) {
                                    Image(systemName: "flag.fill")
                                        .font(.system(size: 18))
                                        .foregroundColor(.red)
                                        .padding(.leading, 8)
                                }
                            }
                            .padding(.leading, H_PADDING)
                        }
                        .padding(.top, 10)
                        .padding(.bottom, 11)
                    }
                )
                .offset(x: xSwipeOffset)
                // .background(Color.white.opacity(0.001)) // Without background DnD does not work. WTF?! Work after highPriorityGesture
                .highPriorityGesture(gesture)
                //                .gesture(gesture)
                .buttonStyle(taskRowButtonStyle)
                .foregroundColor(.primary)
                .background(GeometryReader { geometry -> Color in
                    // Or "Modifying state during view update, this will cause undefined behavior."
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                        itemHeight = geometry.size.height
                    }
                    return Color.clear
                })
            }
            
            if (withDivider) {
                Divider()
                    .padding(.leading, H_PADDING)
            }
        }
    }
    
    // https://stackoverflow.com/a/79037514
    var gesture: some Gesture {
        DragGesture(minimumDistance: 25, coordinateSpace: .global)
            .onChanged { value in
                xSwipeOffset = value.translation.width
                if xSwipeOffset > 1 {
                    tasksTabView.onDragMove(curDragItem: dragItem, value: value)
                }
            }
            .onEnded { value in
                let drop = tasksTabView.onDragStop()
                if let drop = drop {
                    xSwipeOffset = 0
                    if drop is TasksTabDropItemCalendar {
                        navigation.fullScreen(withAnimation: false) {
                            EventFormFullScreen(
                                initEventDb: nil,
                                initText: taskVmUi.taskUi.taskDb.text,
                                initTime: nil,
                                onDone: {
                                    taskVmUi.delete()
                                }
                            )
                        }
                    } else if let dropFolder = drop as? TasksTabDropItemTaskFolder {
                        taskVmUi.upFolder(newFolder: dropFolder.taskFolderDb)
                    }
                } else if value.translation.width < -80 {
                    xSwipeOffset = (width ?? 999) * -1
                } else if value.translation.width > 60 {
                    xSwipeOffset = 0
                    navigation.showTaskForm(
                        strategy: TaskFormStrategy.EditTask(taskDb: taskVmUi.taskUi.taskDb)
                    )
                } else {
                    xSwipeOffset = 0
                }
            }
    }
}

private struct TmrwTaskView: View {
    
    let taskUi: TasksTabTasksVm.TmrwTaskUi
    
    var body: some View {
        
        VStack {
            
            let vPadding = 8.0
            
            if let timeUI = taskUi.timeUi {
                HStack {
                    Text(timeUI.text)
                        .padding(.top, 1)
                        .padding(.bottom, vPadding)
                        .font(.system(size: 14, weight: .light))
                        .foregroundColor(timeUI.textColor.toColor())
                        .lineLimit(1)
                    Spacer()
                }
            }
            
            HStack {
                
                Text(taskUi.text)
                    .lineSpacing(4)
                    .textAlign(.leading)
                
                Spacer()
                
                TriggersIconsView(
                    checklistsDb: taskUi.textFeatures.checklists,
                    shortcutsDb: taskUi.textFeatures.shortcuts
                )
                
            }
        }
        .padding(.leading, H_PADDING)
        .padding(.vertical, 10)
    }
}

private struct MyButtonStyle: ButtonStyle {
    func makeBody(configuration: Self.Configuration) -> some View {
        configuration
            .label
            .background(configuration.isPressed ? Color(.systemGray5) : .black)
    }
}
