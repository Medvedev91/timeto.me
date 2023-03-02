import SwiftUI
import shared

@main
struct W_App: App {

    @State private var vm = WatchAppVM()

    @Environment(\.scenePhase) private var scenePhase
    @WKApplicationDelegateAdaptor(W_Delegate.self) var delegate

    init() {
        UtilsPlatformKt.doInitKmmWatchOS(deviceName: machineIdentifier())
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
