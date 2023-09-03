import SwiftUI
import shared

struct TasksListView: View {

    @State private var vm: TasksListVM

    private let activeFolder: TaskFolderModel
    let tabTasksView: TasksView

    /// hideKeyboard() is more reliable than false
    @FocusState private var isAddFormFocused: Bool

    private let LIST_BOTTOM_ITEM_ID = "bottom_id"

    @StateObject private var keyboardManager = KeyboardManager()

    init(activeFolder: TaskFolderModel, tabTasksView: TasksView) {
        self.tabTasksView = tabTasksView
        self.activeFolder = activeFolder
        _vm = State(initialValue: TasksListVM(folder: activeFolder))
    }

    var body: some View {

        VMView(vm: vm) { state in

            GeometryReader { geometry in

                ScrollViewReader { scrollProxy in

                    ScrollView(.vertical, showsIndicators: false) {

                        VStack {

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
                                                    .padding(.leading, H_PADDING)
                                        }
                                    }
                                }

                                DividerBg()
                                        .padding(.horizontal, 80)
                                        .padding(.top, 20)
                                        .padding(.bottom, state.tasksUI.isEmpty ? 0 : 20)
                            }

                            let tasksUIReversed = state.tasksUI.reversed()
                            VStack {
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
                                    .overlay(squircleShape.stroke(c.dividerBg, lineWidth: onePx))
                                    .padding(.leading, H_PADDING - 2)
                                    .padding(.top, 20)
                                    .padding(.bottom, 20)

                            HStack {
                            }
                                    .id(LIST_BOTTOM_ITEM_ID)
                        }
                                .frame(minHeight: geometry.size.height)
                    }
                            .animation(tabTasksView.withListAnimation ? Animation.easeOut(duration: 0.25) : nil)
                            .offset(y: keyboardManager.height > 0 && isAddFormFocused ? -(keyboardManager.height - bottomNavigationHeight) : 0)
                            ///
                            .onChange(of: isAddFormFocused) { _ in
                                scrollDown(scrollProxy: scrollProxy, toAnimate: true)
                            }
                            .onAppear {
                                scrollDown(scrollProxy: scrollProxy, toAnimate: false)
                            }
                }
            }
                    .padding(.trailing, H_PADDING)
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

private let taskRowButtonStyle = TasksView__TaskRowView.MyButtonStyle()

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
                    .background(configuration.isPressed ? Color(.systemGray5) : c.bg)
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
                                    onStarted: {},
                                    activitiesSheet: {
                                        timetoSheet.showActivitiesTimerSheet(
                                                isPresented: $isSheetPresented,
                                                timerContext: taskUI.timerContext,
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
                                                timerContext: taskUI.timerContext,
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
                            VStack {

                                let vPadding = 8.0

                                if let timeUI = taskUI.timeUI as? TasksListVM.TaskUITimeUIHighlightUI {

                                    HStack {

                                        HStack {

                                            Image(systemName: "calendar")
                                                    .foregroundColor(.white)
                                                    .font(.system(size: 16))
                                                    .padding(.trailing, 3)

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
                                            .padding(.bottom, vPadding - 2)
                                            .padding(.leading, H_PADDING - 1)

                                } else if let timeUI = taskUI.timeUI as? TasksListVM.TaskUITimeUIRegularUI {
                                    HStack {
                                        Text(timeUI.text)
                                                .padding(.leading, H_PADDING)
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

                                    if (taskUI.textFeatures.isImportant) {
                                        Image(systemName: "flag.fill")
                                                .font(.system(size: 18))
                                                .foregroundColor(c.red)
                                                .padding(.leading, 8)
                                    }
                                }
                                        .padding(.leading, H_PADDING)
                            }
                                    .padding(.vertical, 10)
                        }
                )
                        .offset(x: xSwipeOffset)
                        // .background(Color.white.opacity(0.001)) // Without background DnD does not work. WTF?! Work after highPriorityGesture
                        .highPriorityGesture(gesture)
                        .buttonStyle(taskRowButtonStyle)
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
                        .padding(.leading, H_PADDING)
            }
        }
                .id("\(taskUI.task.id) \(taskUI.task.text)") /// #TruncationDynamic
                .sheetEnv(
                        isPresented: $isAddCalendarSheetPresented,
                        content: {
                            EventFormSheet(
                                    isPresented: $isAddCalendarSheetPresented,
                                    editedEvent: nil,
                                    defText: taskUI.task.text,
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

private struct TasksListView__TmrwTaskView: View {

    let taskUI: TasksListVM.TmrwTaskUI

    var body: some View {

        VStack {

            let vPadding = 8.0

            if let timeUI = taskUI.timeUI {
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

                Text(taskUI.text)
                        .lineSpacing(4)
                        .multilineTextAlignment(.leading)
                        .myMultilineText()

                Spacer()

                TriggersListIconsView(triggers: taskUI.textFeatures.triggers, fontSize: 15)
            }
        }
                .padding(.leading, H_PADDING)
                .padding(.vertical, 10)
    }
}
