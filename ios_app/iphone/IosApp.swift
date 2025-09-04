import SwiftUI
import shared

@main
struct IosApp: App {
    
    @Environment(\.scenePhase) private var scenePhase
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @StateObject private var inAppNotificationDelegate = InAppNotificationDelegate()
    
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
                        .attachAutoBackupIos()
                        .statusBar(hidden: true)
                        .onAppear {
                            LiveActivityManager.setup()
                            // Use together
                            UNUserNotificationCenter
                                .current()
                                .requestAuthorization(options: [.badge, .sound, .alert]) { isGranted, _ in
                                    if isGranted {
                                        NotificationsPermission.granted.emit()
                                    } else {
                                        NotificationsPermission.denied.emit()
                                    }
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
                // Cover the case when a user enables notifications from settings.
                // WARNING App not restarts after permission changes.
                if let flowPermission = NotificationsPermission.companion.flow.value as? NotificationsPermission {
                    UNUserNotificationCenter.current().getNotificationSettings { settings in
                        // It only makes sense to check authorized and denied.
                        let status = settings.authorizationStatus
                        if status == .authorized && flowPermission != NotificationsPermission.granted {
                            NotificationsPermission.granted.emit()
                        }
                        else if status == .denied && flowPermission != NotificationsPermission.denied {
                            NotificationsPermission.denied.emit()
                        }
                    }
                }
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
