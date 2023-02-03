import SwiftUI
import shared

struct TabsView: View {

    @State private var vm = TabsVM()

    @EnvironmentObject private var diApple: DIApple

    static var lastInstance: TabsView? = nil

    /// https://stackoverflow.com/a/59972635
    static var tabHeight: CGFloat = 0

    static let TAB_ID_TIMER = 1
    static let TAB_ID_TASKS = 2
    static let TAB_ID_TOOLS = 3
    /// https://stackoverflow.com/a/65048085/5169420
    @State var tabSelection = TAB_ID_TIMER
    private var tabSelectionHandler: Binding<Int> {
        Binding(
                get: { tabSelection },
                set: {
                    /// Repeated click on "Tasks"
                    if tabSelection == TabsView.TAB_ID_TASKS {
                        TabTasksView.lastInstance?.activeSection = TabTasksView_Section_Folder(folder: TaskFolderModel.Companion().getToday())
                    }
                    tabSelection = $0
                }
        )
    }

    /// Created to avoid changing UI while backup
    @State var loadingView: AnyView?

    @State private var triggersChecklist: ChecklistModel?
    @State private var isTriggersChecklistPresented = false

    @EnvironmentObject private var timetoAlert: TimetoAlert

    var body: some View {
        if let loadingView = loadingView {
            loadingView
        } else {
            tabsBody
        }
    }

    private var tabsBody: some View {

        VMView(vm: vm) { state in

            TabView(selection: tabSelectionHandler) {

                /// # PROVOKE_STATE_UPDATE
                EmptyView().id("MainView checklist \(triggersChecklist?.id ?? 0)")

                TabTimerView()
                        .background(TabBarAccessor { tabBar in
                            TabsView.tabHeight = tabBar.bounds.height
                        })
                        .tabItem {
                            Image(systemName: "timer")
                            Text("Timer")
                        }
                        .tag(TabsView.TAB_ID_TIMER)

                TabTasksView()
                        .tabItem {
                            Image(systemName: "tray.full")
                                    // Иначе заполненное изображение т.е. tray.full.full
                                    .environment(\.symbolVariants, .none)
                            Text("Tasks")
                        }
                        .tag(TabsView.TAB_ID_TASKS)
                        .badge(state.todayBadge.toInt())

                TabToolsView()
                        .tabItem {
                            // 1. SF Symbols -> File -> Export Template
                            // 2. Xcode -> Assets -> Symbol Image Set
                            // Just export, import, up .font(...)
                            //                        Image("my_wrench.and.screwdriver")
                            //                                .font(.system(size: 20, weight: .semibold))
                            Image(systemName: "gearshape")
                                    .environment(\.symbolVariants, .none)
                            Text("Tools")
                        }
                        .tag(TabsView.TAB_ID_TOOLS)
            }
                    .onChange(of: diApple.lastInterval?.id) { _ in
                        /// #GD AUTOSTART_TRIGGERS
                        if let lastInterval = diApple.lastInterval, (lastInterval.id + 3 > time()) {
                            let stringToCheckTriggers = lastInterval.note ?? lastInterval.getActivityDI().name
                            guard let trigger = TextFeatures.companion.parse(initText: stringToCheckTriggers).triggers.first else {
                                return
                            }
                            if let trigger = trigger as? Trigger.Checklist {
                                triggersChecklist = trigger.checklist
                                isTriggersChecklistPresented = true
                                return
                            }
                            if let trigger = trigger as? Trigger.Shortcut {
                                performShortcutOrError(trigger.shortcut) { error in
                                    timetoAlert.alert(error)
                                }
                                return
                            }
                            fatalError("invalid trigger type")
                        }
                    }
                    .sheetEnv(isPresented: $isTriggersChecklistPresented) {
                        if let checklist = triggersChecklist {
                            ChecklistDialog(isPresented: $isTriggersChecklistPresented, checklist: checklist)
                        }
                    }
                    .onAppear {
                        UITabBar.appearance().scrollEdgeAppearance = UITabBarAppearance() /// Иначе прозрачный TabView
                        TabsView.lastInstance = self
                    }
        }
    }
}


private struct TabBarAccessor: UIViewControllerRepresentable {

    var callback: (UITabBar) -> Void

    private let proxyController = ViewController()

    func makeUIViewController(
            context: UIViewControllerRepresentableContext<TabBarAccessor>
    ) -> UIViewController {
        proxyController.callback = callback
        return proxyController
    }

    func updateUIViewController(
            _ uiViewController: UIViewController,
            context: UIViewControllerRepresentableContext<TabBarAccessor>
    ) {
    }

    typealias UIViewControllerType = UIViewController

    private class ViewController: UIViewController {

        var callback: (UITabBar) -> Void = { _ in
        }

        override func viewWillAppear(_ animated: Bool) {
            super.viewWillAppear(animated)
            if let tabBar = tabBarController {
                callback(tabBar.tabBar)
            }
        }
    }
}
