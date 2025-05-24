import SwiftUI
import Combine
import shared

private let shortcutPublisher: AnyPublisher<ShortcutDb, Never> = ShortcutPerformer.shared.flow.toPublisher()

struct MainScreen: View {
    
    @State private var tab: MainTabEnum = .home
    
    @State private var isShortcutErrorPresented: Bool = false
    
    var body: some View {
        
        ZStack(alignment: .bottom) {
            
            switch tab {
            case .home:
                HomeScreen()
                    .attachNavigation()
            case .activities:
                ActivitiesScreen(tab: $tab)
                    .attachNavigation()
            case .tasks:
                TasksTabView()
                    .attachNavigation()
            case .settings:
                SettingsScreen()
                    .attachNavigation()
            }
            
            MainTabsView(tab: $tab)
        }
        .ignoresSafeArea(.keyboard) // Hide tab bar under the keyboard
        .onReceive(shortcutPublisher) { shortcutDb in
            guard let swiftUrl = URL(string: shortcutDb.uri), UIApplication.shared.canOpenURL(swiftUrl) else {
                isShortcutErrorPresented = true
                return
            }
            UIApplication.shared.open(swiftUrl)
        }
        .alert(
            "Invalid shortcut link",
            isPresented: $isShortcutErrorPresented,
            actions: {
                Button("Ok", role: .cancel) {
                }
            }
        )
    }
}
