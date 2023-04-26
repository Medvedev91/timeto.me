import SwiftUI
import Combine
import shared

struct TabsView: View {

    @State private var vm = TabsVM()

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
                        TabTasksView.lastInstance?.activeSection = TabTasksView_Section_Folder(folder: DI.getTodayFolder())
                    }
                    tabSelection = $0
                }
        )
    }

    /// Created to avoid changing UI while backup
    @State var loadingView: AnyView?

    @State private var triggersChecklist: ChecklistModel?
    @State private var isTriggersChecklistPresented = false

    private let shortcutPublisher: AnyPublisher<ShortcutModel, Never> = UtilsKt.uiShortcutFlow.toPublisher()
    private let checklistPublisher: AnyPublisher<ChecklistModel, Never> = UtilsKt.uiChecklistFlow.toPublisher()

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
                    .onReceive(shortcutPublisher) { shortcut in
                        let swiftURL = URL(string: shortcut.uri)!
                        if !UIApplication.shared.canOpenURL(swiftURL) {
                            UtilsKt.showUiAlert(message: "Invalid shortcut link", reportApiText: nil)
                            return
                        }
                        UIApplication.shared.open(swiftURL)
                    }
                    .onReceive(checklistPublisher) { checklist in
                        triggersChecklist = checklist
                        isTriggersChecklistPresented = true
                    }
                    .sheetEnv(isPresented: $isTriggersChecklistPresented) {
                        if let checklist = triggersChecklist {
                            ChecklistDialog(isPresented: $isTriggersChecklistPresented, checklist: checklist)
                        }
                    }
                    .onAppear {
                        UITabBar.appearance().scrollEdgeAppearance = UITabBarAppearance() /// Fix transparent TabView
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
