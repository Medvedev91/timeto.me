import SwiftUI
import Combine
import shared

private let shortcutPublisher: AnyPublisher<ShortcutDb, Never> = Utils_kmpKt.uiShortcutFlow.toPublisher()
// todo remove
private let checklistPublisher: AnyPublisher<ChecklistDb, Never> = Utils_kmpKt.uiChecklistFlow.toPublisher()

struct MainScreen: View {
    
    // todo remove
    @State private var triggersChecklist: ChecklistDb?
    // todo remove
    @State private var isTriggersChecklistPresented = false
    
    @State private var tabSelected: MainTabEnum = .home
    
    var body: some View {
        
        VmView({ HomeVm() }) { vm, state in
            
            ZStack(alignment: .bottom) {
                
                // todo remove PROVOKE_STATE_UPDATE
                EmptyView().id("MainView checklist \(triggersChecklist?.id ?? 0)")
                
                switch tabSelected {
                case .home:
                    HomeScreen(vm: vm, state: state)
                        .attachNavigation()
                case .tasks:
                    TasksView()
                        .attachNavigation()
                case .settings:
                    SettingsScreen()
                        .attachNavigation()
                }
                
                HomeTabBar(vm: vm, state: state, tabSelected: $tabSelected)
            }
        }
        .ignoresSafeArea(.keyboard) // To hide tab bar under the keyboard
        .onReceive(shortcutPublisher) { shortcut in
            let swiftURL = URL(string: shortcut.uri)!
            if !UIApplication.shared.canOpenURL(swiftURL) {
                Utils_kmpKt.showUiAlert(message: "Invalid shortcut link", reportApiText: nil)
                return
            }
            UIApplication.shared.open(swiftURL)
        }
        // todo remove AI
        .onReceive(checklistPublisher) { checklist in
            triggersChecklist = checklist
            isTriggersChecklistPresented = true
        }
        // todo remove AI
        .sheetEnv(isPresented: $isTriggersChecklistPresented) {
            if let checklist = triggersChecklist {
                ChecklistSheet(isPresented: $isTriggersChecklistPresented, checklist: checklist)
            }
        }
    }
}
