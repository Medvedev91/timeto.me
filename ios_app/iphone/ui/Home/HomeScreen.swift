import SwiftUI
import Combine
import shared

let HomeScreen__PRIMARY_FONT_SIZE = 18.0

struct HomeScreen: View {
    
    @EnvironmentObject private var nativeSheet: NativeSheet
    @EnvironmentObject private var navigation: Navigation
    
    @State private var triggersChecklist: ChecklistDb?
    @State private var isTriggersChecklistPresented = false
    
    private let shortcutPublisher: AnyPublisher<ShortcutDb, Never> = Utils_kmpKt.uiShortcutFlow.toPublisher()
    private let checklistPublisher: AnyPublisher<ChecklistDb, Never> = Utils_kmpKt.uiChecklistFlow.toPublisher()
    
    var body: some View {
        
        VmView({ HomeVm() }) { vm, state in
            
            ZStack {
                
                // todo PROVOKE_STATE_UPDATE
                EmptyView().id("MainView checklist \(triggersChecklist?.id ?? 0)")
                
                HomeMainTabView(vm: vm, state: state)
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
