import SwiftUI
import shared

@main
struct WatchApp: App {
    
    @Environment(\.scenePhase) private var scenePhase
    @WKApplicationDelegateAdaptor(WatchDelegate.self) var delegate
    
    init() {
        InitKmpWatchOsKt.doInitKmpWatchOs()
    }
    
    var body: some Scene {
        
        WindowGroup {
            VmView({
                WatchAppVm()
            }) { vm, state in
                if state.isAppReady {
                    WatchTabsView()
                        .onChange(of: scenePhase) { _, newScenePhase in
                            if newScenePhase == .active {
                                vm.sync(doForceOrOnce: true)
                            }
                        }
                } else if let syncBtnText = state.syncBtnTextOrNull {
                    Button(syncBtnText) {
                        vm.sync(doForceOrOnce: true)
                    }
                }
            }
        }
    }
}
