import SwiftUI
import shared

private let tabWidth: CGFloat = 34
private let tabShape = RoundedRectangle(cornerRadius: 8, style: .continuous)
private let tabPadding: CGFloat = 15

struct TasksTabView: View {
    
    var body: some View {
        VmView({
            TasksTabVm()
        }) { _, state in
            TasksTabViewInner(
                state: state
            )
        }
    }
}

struct TasksTabViewInner: View {
    
    let state: TasksTabVm.State
    
    ///
    
    @State var section: TasksTabSectionEnum =
        .taskFolder(taskFolderDb: Cache.getTodayFolderDb())
    
    // todo
    
    @State var dropItems: [DropItem] = []
    @State var focusedDrop: DropItem? = nil
    @State var activeDrag: DragItem? = nil
    
    @State private var dropCalendar = DropItem__Calendar()
    
    var body: some View {
        
        ZStack {
            
            Color.black.ignoresSafeArea()
            
            HStack {
                
                switch section {
                case .taskFolder(let taskFolderDb):
                    TasksListView(activeFolder: taskFolderDb, tabTasksView: self)
                        .id("TasksListView \(taskFolderDb.id)") // todo refactoring by vm
                case .repeatings:
                    TasksTabRepeatingsView()
                case .calendar:
                    CalendarTabsView()
                }
                
                VStack {
                    
                    //
                    // Repeating
                    
                    let isActiveRepeating: Bool = if case .repeatings = section { true } else { false }
                    
                    Button(
                        action: {
                            section = .repeatings
                        },
                        label: {
                            Image(systemName: "repeat")
                                .padding(.top, 9)
                                .padding(.bottom, 9)
                                .foregroundColor(isActiveRepeating ? .white : .primary)
                                .font(.system(size: 14, weight: .light))
                        }
                    )
                    .background(
                        RoundedRectangle(cornerRadius: 8, style: .continuous)
                            .fill(isActiveRepeating ? .blue : .black)
                            .frame(width: tabWidth)
                    )
                    
                    //
                    // Folders
                    
                    ForEach(state.taskFoldersUi.reversed(), id: \.taskFolderDb.id) { folderUi in
                        
                        let isActive: Bool = if case .taskFolder(let taskFolderDb) = section {
                            taskFolderDb.id == folderUi.taskFolderDb.id
                        } else { false }
                        
                        TabTasksView__FolderView(
                            isActive: isActive,
                            folderUi: folderUi,
                            tasksTabView: self,
                            drop: DropItem__Folder(folderUi.taskFolderDb)
                        )
                        .padding(.top, tabPadding)
                    }
                    
                    //
                    // Calendar
                    
                    let isActiveCalendar: Bool = if case .calendar = section { true } else { false }
                    
                    TasksCalendarButtonView(
                        isActive: isActiveCalendar,
                        focusedDrop: $focusedDrop,
                        dragItem: $activeDrag,
                        dropItem: dropCalendar,
                        dropItems: $dropItems,
                        onClick: {
                            section = .calendar
                        }
                    )
                    .padding(.top, tabPadding)
                }
                .padding(.trailing, 4)
            }
        }
        .padding(.bottom, MainTabsView__HEIGHT)
        .ignoresSafeArea(.keyboard)
    }
    
    func onDragMove(
        curDragItem: DragItem,
        value: DragGesture.Value
    ) {
        let x = value.location.x
        let y = value.location.y
        focusedDrop = dropItems.first { drop in
            if !curDragItem.isDropAllowed(drop) {
                return false
            }
            let s = drop.square
            return x > s.x1 && y > s.y1 && x < s.x2 && y < s.y2
        }
        activeDrag = curDragItem
    }
    
    func onDragStop() -> DropItem? {
        let dropSave: DropItem? = focusedDrop
        focusedDrop = nil
        activeDrag = nil
        return dropSave
    }
}

private struct TabTasksView__FolderView: View {

    let isActive: Bool
    let folderUi: TasksTabVm.TaskFolderUi
    let tasksTabView: TasksTabViewInner
    @State var drop: DropItem__Folder
    
    ///

    private var isAllowedForDrop: Bool {
        tasksTabView.activeDrag?.isDropAllowed(drop) == true
    }
    
    private var bgColor: Color {
        if (tasksTabView.focusedDrop as? DropItem__Folder)?.folder.id == folderUi.taskFolderDb.id { .green }
        else if isAllowedForDrop { .purple }
        else { isActive ? .blue : .black }
    }
    
    var body: some View {
        Button(
            action: {
                tasksTabView.section = .taskFolder(taskFolderDb: folderUi.taskFolderDb)
            },
            label: {
                
                VStack {
                    
                    Text(folderUi.tabText)
                        .textCase(.uppercase)
                        .lineSpacing(0)
                        .font(.system(size: 14, weight: isActive ? .semibold : .regular, design: .monospaced))
                        .frame(width: tabWidth)
                        .foregroundColor(isActive || isAllowedForDrop ? .white : .primary)
                        .padding(.top, 8)
                        .padding(.bottom, 8)
                }
                .background(tabShape.fill(bgColor))
                .background(GeometryReader { geometry -> Color in
                    drop.square.upByRect(rect: geometry.frame(in: CoordinateSpace.global))
                    return Color.clear
                })
                ///
                .onAppear {
                    tasksTabView.dropItems.append(drop)
                }
                .onDisappear {
                    tasksTabView.dropItems.removeAll {
                        $0 === drop
                    }
                }
            }
        )
    }
}

///
/// Drag and Drop

struct DragItem {

    let isDropAllowed: (_ drop: DropItem) -> Bool
}

class DropItem: ObservableObject {

    let name: String
    let square: Square

    init(name: String, square: Square) {
        self.name = name
        self.square = square
    }

    class Square {

        var x1: CGFloat
        var y1: CGFloat
        var x2: CGFloat
        var y2: CGFloat

        init(x1: CGFloat = 0, y1: CGFloat = 0, x2: CGFloat = 0, y2: CGFloat = 0) {
            self.x1 = x1
            self.y1 = y1
            self.x2 = x2
            self.y2 = y2
        }

        func upByRect(rect: CGRect) {
            x1 = rect.origin.x
            y1 = rect.origin.y
            x2 = rect.origin.x + rect.width
            y2 = rect.origin.y + rect.height
        }
    }
}

class DropItem__Calendar: DropItem {

    init() {
        super.init(name: "Calendar", square: DropItem.Square())
    }
}

class DropItem__Folder: DropItem {

    let folder: TaskFolderDb

    init(_ folder: TaskFolderDb) {
        self.folder = folder
        super.init(name: folder.name, square: DropItem.Square())
    }
}

//
// Calendar Button

private let calendarDots: [[Bool]] = [
    [false, true, true, true],
    [true, true, true, true],
    [true, true, true, false],
]

private struct TasksCalendarButtonView: View {

    let isActive: Bool
    @Binding var focusedDrop: DropItem?
    @Binding var dragItem: DragItem?
    let dropItem: DropItem
    @Binding var dropItems: [DropItem]
    let onClick: () -> Void
    
    ///
    
    private var isAllowedToDrop: Bool {
        dragItem?.isDropAllowed(dropItem) == true
    }
    
    private var isFocusedToDrop: Bool {
        focusedDrop is DropItem__Calendar
    }
    
    private var bgColor: Color {
        if isFocusedToDrop { .green }
        else if isAllowedToDrop { .purple }
        else if isActive { .blue }
        else { .clear }
    }
    
    private var fgColor: Color {
        if isFocusedToDrop { .white }
        else if isAllowedToDrop { .white }
        else if isActive { .white }
        else { .primary }
    }
    
    var body: some View {
        
        Button(
            action: {
                onClick()
            },
            label: {

                GeometryReader { geometry in

                    let _ = dropItem.square.upByRect(rect: geometry.frame(in: CoordinateSpace.global))

                    VStack {

                        ForEachIndexed(calendarDots) { dotsIdx, dots in

                            HStack {

                                ForEachIndexed(dots) { dotIdx, dot in

                                    Spacer()

                                    ZStack {
                                    }
                                    .frame(width: 2 + onePx, height: 2 + onePx)
                                    .background(roundedShape.fill(dot ? fgColor : .clear))
                                }

                                Spacer()
                            }
                            .id("TasksCalendarButtonView dots \(dotsIdx)")
                            .padding(.top, (dotsIdx == 0 ? 1 : 5))
                            .padding(.horizontal, 5)
                        }
                    }
                    .frame(width: tabWidth, height: tabWidth)
                    .background(tabShape.fill(bgColor))
                    ///
                    .onAppear {
                        dropItems.append(dropItem)
                    }
                    .onDisappear {
                        dropItems.removeAll {
                            $0 === dropItem
                        }
                    }
                    ///
                }
                .frame(width: tabWidth, height: tabWidth)
                .padding(.top, 2)
            }
        )
    }
}
