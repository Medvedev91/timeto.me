import SwiftUI
import shared

struct TasksListView: View {

    @State private var vm: TasksListVM

    private let activeFolder: TaskFolderModel
    let tabTasksView: TabTasksView

    /// hideKeyboard() is more reliable than false
    @FocusState private var isAddFormFocused: Bool

    private let LIST_BOTTOM_ITEM_ID = "bottom_id"

    @StateObject private var keyboardManager = KeyboardManager()

    init(activeFolder: TaskFolderModel, tabTasksView: TabTasksView) {
        self.tabTasksView = tabTasksView
        self.activeFolder = activeFolder
        _vm = State(initialValue: TasksListVM(folder: activeFolder))
    }

    var body: some View {

        VMView(vm: vm) { state in

            GeometryReader { geometry in

                ScrollViewReader { scrollProxy in

                    ScrollView(.vertical, showsIndicators: false) {

                        VStack(spacing: 0) {

                            Spacer()

                            if let tmrwData = state.tmrwData {
                                let tmrwTasksUI = tmrwData.tasksUI.reversed()
                                ForEach(tmrwTasksUI, id: \.task.id) { taskUI in
                                    let isFirst = tmrwTasksUI.first == taskUI
                                    ZStack(alignment: .top) {
                                        TasksListView__TmrwTaskView(taskUI: taskUI)
                                                .id("tmrw \(taskUI.task.id)")
                                        if !isFirst {
                                            DividerBg()
                                                    .padding(.leading, TAB_TASKS_PADDING_HALF_H)
                                        }
                                    }
                                }

                                MyDivider()
                                        .padding(.horizontal, 80)
                                        .padding(.top, 20)
                                        .padding(.bottom, state.tasksUI.isEmpty ? 0 : 20)
                            }

                            let tasksUIReversed = state.tasksUI.reversed()
                            VStack(spacing: 0) {
                                ForEach(tasksUIReversed, id: \.task.id) { taskUI in
                                    TasksView__TaskRowView(
                                            taskUI: taskUI,
                                            tasksListView: self,
                                            withDivider: tasksUIReversed.last != taskUI
                                    )
                                            .id(taskUI.task.id)
                                }
                            }

                            if let tmrwData = state.tmrwData {
                                HStack {
                                    Spacer()
                                    Text(tmrwData.curTimeString)
                                            .font(.system(size: 14, weight: .light))

                                    Spacer()
                                }
                                        .padding(.top, 24)
                            }

                            HStack {

                                ZStack {

                                    TextField__VMState(
                                            text: state.addFormInputTextValue,
                                            placeholder: "Task",
                                            isFocused: $isAddFormFocused,
                                            onValueChanged: { newText in
                                                vm.setAddFormInputTextValue(text: newText)
                                            }
                                    )
                                }
                                        .onTapGesture {
                                            isAddFormFocused = true
                                        }

                                Button(
                                        action: {
                                            /// See onTapGesture / onLongPressGesture
                                        },
                                        label: {
                                            Text("SAVE")
                                                    .padding(.horizontal, 12)
                                                    .font(.system(size: 14, weight: .bold))
                                                    .frame(height: 34)
                                                    .foregroundColor(.white)
                                                    .background(
                                                            RoundedRectangle(cornerRadius: 8, style: .continuous)
                                                                    .fill(.blue)
                                                    )
                                                    /// https://stackoverflow.com/a/58643879
                                                    .onTapGesture {
                                                        addTask(toHideKeyboard: true, scrollProxy: scrollProxy)
                                                    }
                                                    .onLongPressGesture(minimumDuration: 0.1) {
                                                        addTask(toHideKeyboard: false, scrollProxy: scrollProxy)
                                                    }
                                        }
                                )
                                        .padding(.trailing, 5)
                                        .buttonStyle(PlainButtonStyle())
                            }
                                    .overlay(squircleShape.stroke(Color(.dividerBg), lineWidth: onePx))
                                    .padding(.horizontal, TAB_TASKS_PADDING_HALF_H - 4)
                                    .padding(.top, 20)
                                    .padding(.bottom, 20)

                            HStack {
                            }
                                    .id(LIST_BOTTOM_ITEM_ID)
                        }
                                .frame(minHeight: geometry.size.height)
                    }
                            .animation(tabTasksView.withListAnimation ? Animation.easeOut(duration: 0.25) : nil)
                            .offset(y: keyboardManager.height > 0 && isAddFormFocused ? -(keyboardManager.height - TabsView.tabHeight) : 0)
                            ///
                            .onChange(of: isAddFormFocused) { _ in
                                scrollDown(scrollProxy: scrollProxy, toAnimate: true)
                            }
                            .onAppear {
                                scrollDown(scrollProxy: scrollProxy, toAnimate: false)
                            }
                }
            }
                    .padding(.trailing, TAB_TASKS_PADDING_HALF_H)
        }
    }

    private func scrollDown(
            scrollProxy: ScrollViewProxy,
            toAnimate: Bool
    ) {
        if (toAnimate) {
            withAnimation {
                scrollProxy.scrollTo(LIST_BOTTOM_ITEM_ID, anchor: .bottom)
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                withAnimation {
                    scrollProxy.scrollTo(LIST_BOTTOM_ITEM_ID, anchor: .bottom)
                }
            }
        } else {
            scrollProxy.scrollTo(LIST_BOTTOM_ITEM_ID, anchor: .bottom)
        }
    }

    private func addTask(
            toHideKeyboard: Bool,
            scrollProxy: ScrollViewProxy
    ) {
        if vm.isAddFormInputEmpty() {
            if toHideKeyboard {
                hideKeyboard()
            }
            return
        }

        vm.addTask {
            DispatchQueue.main.async {
                if toHideKeyboard {
                    hideKeyboard()
                }
            }
        }

        scrollDown(scrollProxy: scrollProxy, toAnimate: true)
    }
}

struct TasksView__TaskRowView: View {

    @EnvironmentObject private var timetoSheet: TimetoSheet

    private let taskUI: TasksListVM.TaskUI

    let tasksListView: TasksListView
    @State private var dragItem: DragItem

    @State private var isSheetPresented = false

    @State private var isAddCalendarSheetPresented = false
    @State private var isEditTaskPresented = false

    @State private var xSwipeOffset: CGFloat = 0
    @State private var width: CGFloat? = nil

    @State private var itemHeight: CGFloat = 0

    private let withDivider: Bool

    init(taskUI: TasksListVM.TaskUI, tasksListView: TasksListView, withDivider: Bool) {
        self.taskUI = taskUI
        self.tasksListView = tasksListView
        self.withDivider = withDivider

        _dragItem = State(initialValue: DragItem(
                isDropAllowed: { drop in
                    if drop is DropItem__Calendar {
                        return true
                    }
                    if let dropFolder = drop as? DropItem__Folder {
                        return dropFolder.folder.id != taskUI.task.folder_id
                    }
                    fatalError("Unknown tasks list drop type")
                }
        ))
    }

    struct MyButtonStyle: ButtonStyle {
        func makeBody(configuration: Self.Configuration) -> some View {
            configuration
                    .label
                    .background(configuration.isPressed ? Color(.systemGray5) : Color(.bg))
        }
    }

    var body: some View {

        ZStack(alignment: .bottom) {

            GeometryReader { proxy in
                ZStack {
                }
                        .onAppear {
                            width = proxy.size.width
                        }
                        .onChange(of: proxy.frame(in: .global).minY) { _ in
                            xSwipeOffset = 0
                            tasksListView.tabTasksView.onDragStop()
                        }
            }
                    /// height: 1, or full height items if short
                    .frame(height: 1)
            ///

            if (xSwipeOffset > 0) {
                let editOrMoveTitle = tasksListView.tabTasksView.focusedDrop != nil ? "Move to \(tasksListView.tabTasksView.focusedDrop!.name)" : "Edit"
                HStack {
                    Text(editOrMoveTitle)
                            .foregroundColor(.white)
                            .padding(.leading, 16)
                    Spacer()
                }
                        .frame(maxHeight: itemHeight)
                        .background(tasksListView.tabTasksView.focusedDrop == nil ? .blue : .green)
                        .offset(x: xSwipeOffset > 0 ? 0 : xSwipeOffset)
            }

            if (xSwipeOffset < 0) {

                HStack {

                    Text(taskUI.text)
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
                                taskUI.delete()
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

            //////

            ZStack {

                Button(
                        action: {
                            hideKeyboard()
                            taskUI.task.startIntervalForUI(
                                    onStarted: {
                                        tasksListView.tabTasksView.onTaskStarted()
                                    },
                                    activitiesSheet: {
                                        timetoSheet.showActivitiesTimerSheet(
                                                isPresented: $isSheetPresented,
                                                timerContext: taskUI.timerContext,
                                                selectedActivity: nil,
                                                onStart: {
                                                    isSheetPresented = false
                                                    tasksListView.tabTasksView.onTaskStarted()
                                                }
                                        )
                                    },
                                    timerSheet: { activity in
                                        timetoSheet.showActivitiesTimerSheet(
                                                isPresented: $isSheetPresented,
                                                timerContext: taskUI.timerContext,
                                                selectedActivity: activity,
                                                onStart: {
                                                    isSheetPresented = false
                                                    tasksListView.tabTasksView.onTaskStarted()
                                                }
                                        )
                                    }
                            )
                        },
                        label: {
                            VStack(spacing: 0) {

                                let vPadding = 8.0

                                if let timeUI = taskUI.timeUI as? TasksListVM.TaskUITimeUIImportantUI {

                                    HStack(spacing: 0) {

                                        HStack(spacing: 0) {

                                            Image(systemName: "calendar")
                                                    .foregroundColor(.white)
                                                    .frame(width: 12, height: 12)
                                                    .padding(.leading, 2)
                                                    .padding(.trailing, 7)

                                            Text(timeUI.title)
                                                    .foregroundColor(.white)
                                                    .font(.system(size: 13))
                                        }
                                                .padding(.leading, 6)
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
                                            .padding(.bottom, vPadding - 2)
                                            .padding(.leading, TAB_TASKS_PADDING_HALF_H - 1)

                                } else if let timeUI = taskUI.timeUI as? TasksListVM.TaskUITimeUIRegularUI {
                                    HStack {
                                        Text(timeUI.text)
                                                .padding(.leading, TAB_TASKS_PADDING_HALF_H)
                                                .padding(.top, 1)
                                                .padding(.bottom, vPadding)
                                                .font(.system(size: 14, weight: .light))
                                                .foregroundColor(timeUI.textColor.toColor())
                                                .lineLimit(1)
                                        Spacer()
                                    }
                                }

                                HStack {

                                    Text(taskUI.text)
                                            .lineSpacing(4)
                                            .multilineTextAlignment(.leading)
                                            .myMultilineText()

                                    Spacer()

                                    TriggersListIconsView(triggers: taskUI.textFeatures.triggers, fontSize: 15)
                                }
                                        .padding(.leading, TAB_TASKS_PADDING_HALF_H)
                            }
                                    .padding(.vertical, 10)
                        }
                )
                        .offset(x: xSwipeOffset)
                        // .background(Color.white.opacity(0.001)) // Without background DnD does not work. WTF?! Work after highPriorityGesture
                        .highPriorityGesture(gesture)
                        .buttonStyle(MyButtonStyle())
                        .foregroundColor(.primary)
                        .background(GeometryReader { geometry -> Color in
                            /// Or "Modifying state during view update, this will cause undefined behavior."
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                                itemHeight = geometry.size.height
                            }
                            return Color.clear
                        })
            }

            if (withDivider) {
                DividerBg()
                        .padding(.leading, TAB_TASKS_PADDING_HALF_H)
            }
        }
                .id("\(taskUI.task.id) \(taskUI.task.text)") /// #TruncationDynamic
                .sheetEnv(
                        isPresented: $isAddCalendarSheetPresented,
                        content: {
                            EventFormSheet(
                                    isPresented: $isAddCalendarSheetPresented,
                                    editedEvent: nil,
                                    defText: taskUI.text,
                                    defDate: Date().startOfDay()
                            ) {
                                taskUI.delete()
                            }
                        }
                )
                .sheetEnv(
                        isPresented: $isEditTaskPresented,
                        content: {
                            TaskFormSheet(
                                    task: taskUI.task,
                                    isPresented: $isEditTaskPresented
                            )
                        }
                )
    }

    var gesture: some Gesture {
        DragGesture(minimumDistance: 15, coordinateSpace: .global)
                .onChanged { value in
                    xSwipeOffset = value.translation.width
                    if xSwipeOffset > 1 {
                        tasksListView.tabTasksView.onDragMove(curDragItem: dragItem, value: value)
                    }
                }
                .onEnded { value in
                    let drop = tasksListView.tabTasksView.onDragStop()
                    if let drop = drop {
                        xSwipeOffset = 0
                        if drop is DropItem__Calendar {
                            isAddCalendarSheetPresented = true
                        } else if let dropFolder = drop as? DropItem__Folder {
                            taskUI.upFolder(newFolder: dropFolder.folder)
                        }
                    } else if value.translation.width < -80 {
                        xSwipeOffset = (width ?? 999) * -1
                    } else if value.translation.width > 60 {
                        xSwipeOffset = 0
                        isEditTaskPresented = true
                    } else {
                        xSwipeOffset = 0
                    }
                }
    }
}

struct TasksView__TaskRowView__ActivityRowView: View {

    var activityUI: ActivitiesTimerSheetVM.ActivityUI
    let onClickOnTimer: () -> Void
    let onStarted: () -> Void

    var body: some View {

        Button(
                action: {
                    onClickOnTimer()
                },
                label: {

                    ZStack(alignment: .bottom) { // .bottom for divider

                        let emojiHPadding = 8.0
                        let emojiWidth = 30.0
                        let startPadding = emojiWidth + (emojiHPadding * 2)

                        HStack(spacing: 0) {

                            Text(activityUI.activity.emoji)
                                    .frame(width: emojiWidth)
                                    .padding(.horizontal, emojiHPadding)
                                    .font(.system(size: 22))

                            Text(activityUI.listText)
                                    .foregroundColor(.primary)
                                    .truncationMode(.tail)
                                    .lineLimit(1)

                            Spacer()

                            ForEach(activityUI.timerHints, id: \.seconds) { hintUI in
                                let isPrimary = hintUI.isPrimary
                                Button(
                                        action: {
                                            hintUI.startInterval {
                                                onStarted()
                                            }
                                        },
                                        label: {
                                            Text(hintUI.text)
                                                    .font(.system(size: isPrimary ? 13 : 14, weight: isPrimary ? .medium : .light))
                                                    .foregroundColor(isPrimary ? .white : .blue)
                                                    .padding(.leading, 6)
                                                    .padding(.trailing, isPrimary ? 6 : 2)
                                                    .padding(.top, 3)
                                                    .padding(.bottom, 3.5)
                                                    .background(isPrimary ? .blue : .clear)
                                                    .cornerRadius(99)
                                                    .padding(.leading, isPrimary ? 4 : 0)
                                        }
                                )
                                        .buttonStyle(.borderless)
                            }
                        }
                                .padding(.trailing, 14)
                                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)

                        MyDivider(xOffset: startPadding)
                    }
                            .frame(alignment: .bottom)
                            .padding(.leading, 2)
                }
        )
    }
}

private struct TasksListView__TmrwTaskView: View {

    let taskUI: TasksListVM.TmrwTaskUI

    var body: some View {

        VStack(spacing: 0) {

            let vPadding = 8.0

            if let timeUI = taskUI.timeUI {
                HStack {
                    Text(timeUI.text)
                            .padding(.leading, TAB_TASKS_PADDING_HALF_H)
                            .padding(.top, 1)
                            .padding(.bottom, vPadding)
                            .font(.system(size: 14, weight: .light))
                            .foregroundColor(timeUI.textColor.toColor())
                            .lineLimit(1)
                    Spacer()
                }
            }

            HStack {

                Text(taskUI.text)
                        .lineSpacing(4)
                        .multilineTextAlignment(.leading)
                        .myMultilineText()

                Spacer()

                TriggersListIconsView(triggers: taskUI.textFeatures.triggers, fontSize: 15)
            }
                    .padding(.leading, TAB_TASKS_PADDING_HALF_H)
        }
                .padding(.vertical, 10)
    }
}
