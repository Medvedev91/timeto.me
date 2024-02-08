import SwiftUI
import shared

private let tabWidth: CGFloat = 34

struct TasksView: View {

    //////

    @State private var vm = TabTasksVM()

    static var lastInstance: TasksView? = nil

    @State var activeSection: TabTasksView_Section? = TabTasksView_Section_Folder(folder: DI.getTodayFolder())

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
                                        .opacity(isActiveRepeating ? 1 : 0.7)

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
                    let calendarFgColor: Color = {
                        if focusedDrop is DropItem__Calendar {
                            return .green
                        }
                        if activeDrag?.isDropAllowed(dropCalendar) == true {
                            return .purple
                        }
                        if isActiveCalendar {
                            return .blue
                        }
                        return c.homeFontSecondary
                    }()

                    Button(
                            action: {
                                upActiveSectionWithAnimation(TabTasksView_Section_Calendar())
                            },
                            label: {
                                GeometryReader { geometry in
                                    let _ = dropCalendar.square.upByRect(rect: geometry.frame(in: CoordinateSpace.global))
                                    Image(systemName: "calendar")
                                            .resizable()
                                            .animation(.spring())
                                            .font(.system(size: 18, weight: .thin))
                                            ///
                                            .onAppear {
                                                dropItems.append(dropCalendar)
                                            }
                                            .onDisappear {
                                                dropItems.removeAll { $0 === dropCalendar }
                                            }
                                            ///
                                            .foregroundColor(calendarFgColor)
                                }
                                        .frame(width: tabWidth - 2.4, height: tabWidth - 2.4)
                            }
                    )
                            .background(
                                    ZStack {
                                        RoundedRectangle(cornerRadius: 4, style: .continuous)
                                                .fill(c.bg)
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
                        activeSection = TabTasksView_Section_Folder(folder: DI.getTodayFolder())
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
    private let folderUI: TabTasksVM.TaskFolderUI
    private let tabTasksView: TasksView

    @State private var drop: DropItem__Folder

    init(
            isActive: Bool,
            folderUI: TabTasksVM.TaskFolderUI,
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
                            .background(RoundedRectangle(cornerRadius: 8, style: .continuous).fill(bgColor))
                            .background(GeometryReader { geometry -> Color in
                                drop.square.upByRect(rect: geometry.frame(in: CoordinateSpace.global))
                                return Color.clear
                            })
                            ///
                            .onAppear {
                                tabTasksView.dropItems.append(drop)
                            }
                            .onDisappear {
                                tabTasksView.dropItems.removeAll { $0 === drop }
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
