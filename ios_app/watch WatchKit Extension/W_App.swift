import SwiftUI
import shared

@main
struct W_App: App {

    @State private var vm = WatchAppVm()

    @Environment(\.scenePhase) private var scenePhase
    @WKApplicationDelegateAdaptor(W_Delegate.self) var delegate

    init() {
        Utils_kmp_watchosKt.doInitKmpWatchOS()
    }

    var body: some Scene {

        WindowGroup {

            VMView(vm: vm) { state in

                if state.isAppReady {
                    W_TabsView()
                } else if let syncBtnText = state.syncBtnTextOrNull {
                    Button(syncBtnText) {
                        vm.sync(doForceOrOnce: true)
                    }
                }
            }
        }
                .onChange(of: scenePhase) { newScenePhase in
                    if newScenePhase == .active {
                        vm.sync(doForceOrOnce: true)
                    }
                }
    }
}
