import SwiftUI
import shared

@main
struct W_App: App {

    @Environment(\.scenePhase) private var scenePhase
    @WKApplicationDelegateAdaptor(W_Delegate.self) var delegate

    @State private var vm: WatchAppVM

    init() {
        UtilsPlatformKt.doInitKmmWatchOS(deviceName: machineIdentifier())
        _vm = State(initialValue: WatchAppVM())
    }

    var body: some Scene {

        WindowGroup {

            VMView(vm: vm) { state in

                if state.isAppReady {
                    W_TabsView()
                            .attachDIApple()
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
