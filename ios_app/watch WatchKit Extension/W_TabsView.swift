import SwiftUI
import Combine
import shared

struct W_TabsView: View {
    
    static var lastInstance: W_TabsView? = nil
    
    static let TAB_ID_TIMER = 1
    static let TAB_ID_TASKS = 2
    
    @State var tabSelection = TAB_ID_TIMER
    
    private let alertPublisher: AnyPublisher<UIAlertData, Never> = Utils_kmpKt.uiAlertFlow.toPublisher()
    @State private var dialogErrorText: String?
    @State private var dialogErrorIsPresented = false
    
    var body: some View {
        
        let _ = W_TabsView.lastInstance = self
        
        TabView(selection: $tabSelection) {
            
            WatchTabTimerView()
                .tag(W_TabsView.TAB_ID_TIMER)
            
            W_TabTasksView()
                .tag(W_TabsView.TAB_ID_TASKS)
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
