import SwiftUI
import shared

let TAB_TASKS_PADDING_HALF_H = 16.0

private let tabWidth: CGFloat = 34

// TRICK Using ignoresSafeArea() outside is mandatory
struct TabTasksView: View {

    let withRepeatings: Bool
    let onTaskStarted: () -> Void

    //////

    @State private var vm = TabTasksVM()

    static var lastInstance: TabTasksView? = nil

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

            Color(.bg)
                    .ignoresSafeArea()

            HStack {

                /// Because of upActiveSectionWithAnimation() without Spacer can be twitching
                Spacer()

                if let section = activeSection as? TabTasksView_Section_Folder {
                    /// OMG! Dirty trick!
                    /// Just TabTaskView_TasksListView(...) doesn't call onAppear() to scroll to the bottom.
                    ForEach(state.folders, id: \.id) { folder in
                        if section.folder.id == folder.id {
                            TasksListView(activeFolder: section.folder, tabTasksView: self)
                        }
                    }
                } else if activeSection is TabTasksView_Section_Repeating {
                    RepeatingsListView()
                } else if activeSection is TabTasksView_Section_Calendar {
                    EventsListView()
                }

                VStack {

                    //
                    // Calendar

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
                        return Color(UIColor(argb: 0xFF5F5F5F))
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
                                                .fill(Color(.myDayNight(.mySecondaryBackground, .myBackground)))
                                    }
                            )

                    //
                    // Repeating

                    if withRepeatings {

                        Spacer()
                                .frame(height: tabPadding)

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

                                        ZStack {

                                            RoundedRectangle(cornerRadius: 8, style: .continuous)
                                                    .fill(isActiveRepeating ? .blue : Color(.bg))

                                            RoundedRectangle(cornerRadius: 8, style: .continuous)
                                                    .stroke(isActiveRepeating ? .blue : Color(.dividerBg), lineWidth: onePx)
                                        }
                                                .frame(width: tabWidth)
                                )
                    }

                    //
                    // Folders

                    ForEach(state.folders.reversed(), id: \.id) { folder in

                        let isActive = folder.id == (activeSection as? TabTasksView_Section_Folder)?.folder.id

                        Spacer()
                                .frame(height: tabPadding)

                        TabTasksView__FolderView(
                                isActive: isActive,
                                folder: folder,
                                tabTasksView: self
                        )
                    }
                }
                        .padding(.trailing, 10)
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
                    TabTasksView.lastInstance = self
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
    private let folder: TaskFolderModel
    private let tabTasksView: TabTasksView

    @State private var drop: DropItem__Folder

    init(
            isActive: Bool,
            folder: TaskFolderModel,
            tabTasksView: TabTasksView
    ) {
        self.isActive = isActive
        self.folder = folder
        self.tabTasksView = tabTasksView
        _drop = State(initialValue: DropItem__Folder(folder))
    }

    var body: some View {
        Button(
                action: {
                    tabTasksView.upActiveSectionWithAnimation(TabTasksView_Section_Folder(folder: folder))
                },
                label: {
                    let nameN = Array(folder.name)
                            .map { String($0) }
                            .joined(separator: "\n")

                    let isAllowedForDrop = tabTasksView.activeDrag?.isDropAllowed(drop) == true
                    let bgColor: Color = {
                        if (tabTasksView.focusedDrop as? DropItem__Folder)?.folder.id == folder.id {
                            return .green
                        }
                        if isAllowedForDrop {
                            return .purple
                        }
                        return isActive ? .blue : Color(.bg)
                    }()

                    VStack {

                        Text(nameN)
                                .textCase(.uppercase)
                                .lineSpacing(0)
                                .font(.system(size: 14, weight: isActive ? .semibold : .regular, design: .monospaced))
                                .frame(width: tabWidth)
                                .foregroundColor(isActive || isAllowedForDrop ? .white : .primary)
                                ///
                                .padding(.top, 8)
                                .padding(.bottom, 8)
                    }
                            .background(
                                    ZStack {
                                        RoundedRectangle(cornerRadius: 8, style: .continuous)
                                                .fill(bgColor)

                                        RoundedRectangle(cornerRadius: 8, style: .continuous)
                                                .stroke(isActive ? .blue : Color(.dividerBg), lineWidth: onePx)
                                    }
                            )
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

    let folder: TaskFolderModel

    init(_ folder: TaskFolderModel) {
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
    let folder: TaskFolderModel
}

struct TabTasksView_Section_Repeating: TabTasksView_Section {
}

struct TabTasksView_Section_Calendar: TabTasksView_Section {
}
