import SwiftUI
import Combine
import shared

let HomeScreen__ITEM_HEIGHT = 38.0
let HomeScreen__PRIMARY_FONT_SIZE = 18.0

private let shortcutPublisher: AnyPublisher<ShortcutDb, Never> = Utils_kmpKt.uiShortcutFlow.toPublisher()
// todo remove
private let checklistPublisher: AnyPublisher<ChecklistDb, Never> = Utils_kmpKt.uiChecklistFlow.toPublisher()

struct HomeScreen: View {
    
    @EnvironmentObject private var navigation: Navigation
    
    @State private var triggersChecklist: ChecklistDb?
    @State private var isTriggersChecklistPresented = false
    
    @State private var tabSelected: HomeTabSelected = .main
    
    var body: some View {
        
        VmView({ HomeVm() }) { vm, state in
            
            ZStack(alignment: .bottom) {
                
                // todo remove PROVOKE_STATE_UPDATE
                EmptyView().id("MainView checklist \(triggersChecklist?.id ?? 0)")
                
                switch tabSelected {
                case .main:
                    HomeMainTabView(vm: vm, state: state)
                        .attachNavigation()
                case .settings:
                    SettingsScreen()
                        .attachNavigation()
                }
                
                HomeTabBar(vm: vm, state: state, tabSelected: $tabSelected)
            }
        }
        .ignoresSafeArea(.keyboard, edges: .bottom)
        .onReceive(shortcutPublisher) { shortcut in
            let swiftURL = URL(string: shortcut.uri)!
            if !UIApplication.shared.canOpenURL(swiftURL) {
                Utils_kmpKt.showUiAlert(message: "Invalid shortcut link", reportApiText: nil)
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
                ChecklistSheet(isPresented: $isTriggersChecklistPresented, checklist: checklist)
            }
        }
    }
}
