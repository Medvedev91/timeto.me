import SwiftUI
import shared

private let tabWidth: CGFloat = 34
private let tabShape = RoundedRectangle(cornerRadius: 8, style: .continuous)

struct TasksView: View {

    //////

    @State private var vm = TabTasksVm()

    static var lastInstance: TasksView? = nil

    @State var activeSection: TabTasksView_Section? =
        TabTasksView_Section_Folder(folder: Cache.getTodayFolderDb())

    /// No more fits when the keyboard is open on the SE
    private let tabPadding: CGFloat = 15

    /// Docs in use places
    @State var withListAnimation = true

    @State var dropItems: [DropItem] = []
    @State var focusedDrop: DropItem? = nil
    @State var activeDrag: DragItem? = nil

    @State private var dropCalendar = DropItem__Calendar()

    func onDragMove(curDragItem: DragItem, value: DragGesture.Value) {
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
        let curFocusedDrop = focusedDrop
        focusedDrop = nil
        activeDrag = nil
        return curFocusedDrop
    }

    var body: some View {

        VMView(vm: vm, stack: .ZStack()) { state in

            c.bg.ignoresSafeArea()

            HStack {

                /// Because of upActiveSectionWithAnimation() without Spacer can be twitching
                Spacer()

                if let section = activeSection as? TabTasksView_Section_Folder {
                    /// OMG! Dirty trick!
                    /// Just TabTaskView_TasksListView(...) doesn't call onAppear() to scroll to the bottom.
                    ForEach(state.taskFoldersUI, id: \.folder.id) { folderUI in
                        if section.folder.id == folderUI.folder.id {
                            TasksListView(activeFolder: section.folder, tabTasksView: self)
                        }
                    }
                } else if activeSection is TabTasksView_Section_Repeating {
                    RepeatingsListView()
                } else if activeSection is TabTasksView_Section_Calendar {
                    EventsView()
                }

                VStack {

                    //
                    // Repeating

                    let isActiveRepeating = activeSection is TabTasksView_Section_Repeating

                    Button(
                        action: {
                            upActiveSectionWithAnimation(TabTasksView_Section_Repeating())
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
                            .fill(isActiveRepeating ? .blue : c.bg)
                            .frame(width: tabWidth)
                    )

                    //
                    // Folders

                    ForEach(state.taskFoldersUI.reversed(), id: \.folder.id) { folderUI in

                        let isActive = folderUI.folder.id == (activeSection as? TabTasksView_Section_Folder)?.folder.id

                        Spacer()
                            .frame(height: tabPadding)

                        TabTasksView__FolderView(
                            isActive: isActive,
                            folderUI: folderUI,
                            tabTasksView: self
                        )
                    }

                    //
                    // Calendar

                    Spacer()
                        .frame(height: tabPadding)

                    let isActiveCalendar = activeSection is TabTasksView_Section_Calendar

                    TasksCalendarButtonView(
                        isActive: isActiveCalendar,
                        focusedDrop: $focusedDrop,
                        dragItem: $activeDrag,
                        dropItem: dropCalendar,
                        dropItems: $dropItems,
                        onClick: {
                            upActiveSectionWithAnimation(TabTasksView_Section_Calendar())
                        }
                    )
                }
                .padding(.trailing, 4)
            }
            .onAppear {
                UITableView.appearance().sectionFooterHeight = 0
                UIScrollView.appearance().keyboardDismissMode = .interactive
            }
            .onDisappear {
                /// On onDisappear(), otherwise on onAppear() twitching (hide old and open new).
                activeSection = TabTasksView_Section_Folder(folder: Cache.getTodayFolderDb())
            }
        }
        .onAppear {
            TasksView.lastInstance = self
        }
    }

    func upActiveSectionWithAnimation(
        _ newSection: TabTasksView_Section
    ) {
        /// Fix issue: on tab changes scroll animation.
        withListAnimation = false
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { /// 0.1 на глаз
            withListAnimation = true
        }
        if activeSection is TabTasksView_Section_Folder && newSection is TabTasksView_Section_Folder {
            /// It's better without animation, faster.
            activeSection = newSection
        } else {
            withAnimation(Animation.linear(duration: 0.04)) {
                activeSection = nil
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.03) {
                activeSection = newSection
            }
        }
    }
}

private struct TabTasksView__FolderView: View {

    private let isActive: Bool
    private let folderUI: TabTasksVm.TaskFolderUI
    private let tabTasksView: TasksView

    @State private var drop: DropItem__Folder

    init(
        isActive: Bool,
        folderUI: TabTasksVm.TaskFolderUI,
        tabTasksView: TasksView
    ) {
        self.isActive = isActive
        self.folderUI = folderUI
        self.tabTasksView = tabTasksView
        _drop = State(initialValue: DropItem__Folder(folderUI.folder))
    }

    var body: some View {
        Button(
            action: {
                tabTasksView.upActiveSectionWithAnimation(TabTasksView_Section_Folder(folder: folderUI.folder))
            },
            label: {
                let isAllowedForDrop = tabTasksView.activeDrag?.isDropAllowed(drop) == true
                let bgColor: Color = {
                    if (tabTasksView.focusedDrop as? DropItem__Folder)?.folder.id == folderUI.folder.id {
                        return .green
                    }
                    if isAllowedForDrop {
                        return .purple
                    }
                    return isActive ? .blue : c.bg
                }()

                VStack {

                    Text(folderUI.tabText)
                        .textCase(.uppercase)
                        .lineSpacing(0)
                        .font(.system(size: 14, weight: isActive ? .semibold : .regular, design: .monospaced))
                        .frame(width: tabWidth)
                        .foregroundColor(isActive || isAllowedForDrop ? .white : .primary)
                        ///
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
                    tabTasksView.dropItems.append(drop)
                }
                .onDisappear {
                    tabTasksView.dropItems.removeAll {
                        $0 === drop
                    }
                }
                ///
                .animation(.spring())
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

//////


//
// TabTasksView_Section

protocol TabTasksView_Section {
}

struct TabTasksView_Section_Folder: TabTasksView_Section {
    let folder: TaskFolderDb
}

struct TabTasksView_Section_Repeating: TabTasksView_Section {
}

struct TabTasksView_Section_Calendar: TabTasksView_Section {
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

    var body: some View {

        let isAllowedToDrop = dragItem?.isDropAllowed(dropItem) == true
        let isFocusedToDrop = focusedDrop is DropItem__Calendar

        let bgColor: Color = {
            if isFocusedToDrop {
                return .green
            }
            if isAllowedToDrop {
                return .purple
            }
            if isActive {
                return .blue
            }
            return c.transparent
        }()

        let fgColor: Color = {
            if isFocusedToDrop {
                return c.white
            }
            if isAllowedToDrop {
                return c.white
            }
            if isActive {
                return c.white
            }
            return .primary
        }()

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
                                    .background(roundedShape.fill(dot ? fgColor : c.transparent))
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
