import SwiftUI
import Combine
import shared

@main
struct IosApp: App {
    
    @Environment(\.scenePhase) private var scenePhase
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @StateObject private var inAppNotificationDelegate = InAppNotificationDelegate()
    
    private let scheduledNotificationsDataPublisher: AnyPublisher<NSArray, Never> =
    Utils_kmpKt.scheduledNotificationsDataFlow.toPublisher()
    
    private let keepScreenOnDataPublisher: AnyPublisher<KotlinBoolean, Never> =
    Utils_kmpKt.keepScreenOnStateFlow.toPublisher()
    
    private let batteryManager = BatteryManager() // Keep the object
    
    var body: some Scene {
        
        WindowGroup {
            
            VmView({
                AppVm()
            }) { vm, state in
                
                if let backupMessage = state.backupMessage {
                    BackupMessageView(message: backupMessage)
                } else if state.isAppReady {
                    MainScreen()
                        .attachFs()
                        .attachTimetoAlert()
                        .attachAutoBackupIos()
                        .attachNativeSheet()
                        .statusBar(hidden: true)
                        .onReceive(scheduledNotificationsDataPublisher) {
                            let center = UNUserNotificationCenter.current()
                            center.removeAllPendingNotificationRequests()
                            let dataItems = $0 as! [ScheduledNotificationData]
                            dataItems.forEach { data in
                                schedulePush(data: data)
                            }
                        }
                        .onReceive(keepScreenOnDataPublisher) { keepScreenOn in
                            UIApplication.shared.isIdleTimerDisabled = (keepScreenOn == true)
                        }
                        .onAppear {
                            // Use together
                            UNUserNotificationCenter
                                .current()
                                .requestAuthorization(options: [.badge, .sound, .alert]) { isGranted, _ in
                                    if isGranted {
                                        // Without delay the first event does not handled. 50mls enough.
                                        vm.onNotificationsPermissionReady(delayMls: Int64(500))
                                    }
                                }
                            UNUserNotificationCenter.current().delegate = inAppNotificationDelegate
                        }
                }
            }
        }
        .onChange(of: scenePhase) { _, phase in
            if phase == .active {
                UNUserNotificationCenter.current().removeAllDeliveredNotifications()
                UNUserNotificationCenter.current().setBadgeCount(0)
            }
        }
    }
}

///

private struct BackupMessageView: View {
    
    let message: String
    
    var body: some View {
        ZStack {
            Text(message)
                .foregroundColor(.primary)
        }
        .fillMaxSize()
        .background(.background)
    }
}
