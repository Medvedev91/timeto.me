import SwiftUI
import Combine
import shared

struct WatchTabsView: View {
    
    static var lastInstance: WatchTabsView? = nil
    
    static let TAB_ID_TIMER = 1
    static let TAB_ID_TASKS = 2
    
    @State var tabSelection = TAB_ID_TIMER
    
    private let alertPublisher: AnyPublisher<UIAlertData, Never> = Utils_kmpKt.uiAlertFlow.toPublisher()
    @State private var dialogErrorText: String?
    @State private var dialogErrorIsPresented = false
    
    var body: some View {
        
        let _ = WatchTabsView.lastInstance = self
        
        TabView(selection: $tabSelection) {
            
            WatchTabTimerView()
                .tag(WatchTabsView.TAB_ID_TIMER)
            
            WatchTabTasksView()
                .tag(WatchTabsView.TAB_ID_TASKS)
        }
        /// TRICK. Otherwise the text does not updates
        .onReceive(alertPublisher) { data in
            dialogErrorText = data.message
        }
        .onChange(of: dialogErrorText) { _, newValue in
            dialogErrorIsPresented = newValue != nil
        }
        ///
        .sheet(
            isPresented: $dialogErrorIsPresented,
            onDismiss: { dialogErrorText = nil }
        ) {
            Text(dialogErrorText ?? "")
                .font(.title2)
                .foregroundColor(.red)
        }
    }
}
