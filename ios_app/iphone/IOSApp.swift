import SwiftUI
import Combine
import shared

@main
struct IOSApp: App {

    @State private var vm = AppVM()

    @Environment(\.scenePhase) private var scenePhase
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @StateObject private var myInAppNotificationDelegate = MyInAppNotificationDelegate()

    private let scheduledNotificationsDataPublisher: AnyPublisher<NSArray, Never> =
            UtilsKt.scheduledNotificationsDataFlow.toPublisher()

    private let keepScreenOnDataPublisher: AnyPublisher<KotlinBoolean, Never> =
            UtilsKt.keepScreenOnStateFlow.toPublisher()

    private let batteryManager = BatteryManager() // Keep the object

    init() {
        UtilsPlatformKt.doInitKmmIos(deviceName: machineIdentifier())
    }

    var body: some Scene {

        WindowGroup {

            VMView(vm: vm) { state in

                if state.isAppReady {

                    MainView()
                            .attachTimetoSheet()
                            .attachTimetoAlert()
                            .attachAutoBackupIos()
                            .onReceive(scheduledNotificationsDataPublisher) {
                                let center = UNUserNotificationCenter.current()
                                center.removeAllPendingNotificationRequests()
                                let dataItems = $0 as! [ScheduledNotificationData]
                                dataItems.forEach { data in schedulePush(data: data) }
                            }
                            .onReceive(keepScreenOnDataPublisher) { keepScreenOn in
                                UIApplication.shared.isIdleTimerDisabled = (keepScreenOn == true)
                            }
                            .onAppear {
                                /// Use together
                                UNUserNotificationCenter
                                        .current()
                                        .requestAuthorization(options: [.badge, .sound, .alert]) { isGranted, _ in
                                            if isGranted {
                                                // Without delay the first event does not handled. 50mls enough.
                                                vm.onNotificationsPermissionReady(delayMls: Int64(500))
                                            }
                                        }
                                UNUserNotificationCenter.current().delegate = myInAppNotificationDelegate
                                ///
                            }
                }
            }
        }
                .onChange(of: scenePhase) { phase in
                    // Remove notifications and badges
                    // https://stackoverflow.com/a/41487410
                    // https://betterprogramming.pub/swiftui-tips-detecting-a-swiftui-apps-active-inactive-and-background-state-a5ff8acf5db1
                    if phase == .active {
                        UNUserNotificationCenter.current().removeAllDeliveredNotifications()
                        UIApplication.shared.applicationIconBadgeNumber = 0
                    }
                }
    }
}

private class BatteryManager {

    init() {
        UIDevice.current.isBatteryMonitoringEnabled = true
        BatteryManager.upBatteryState()
        BatteryManager.upBatteryLevel()
        NotificationCenter.default.addObserver(
                self,
                selector: #selector(batteryStateDidChange(notification:)),
                name: UIDevice.batteryStateDidChangeNotification,
                object: nil
        )
        NotificationCenter.default.addObserver(
                self,
                selector: #selector(batteryLevelDidChange(notification:)),
                name: UIDevice.batteryLevelDidChangeNotification,
                object: nil
        )
    }

    @objc private func batteryStateDidChange(notification: Notification) {
        BatteryManager.upBatteryState()
    }

    @objc private func batteryLevelDidChange(notification: Notification) {
        BatteryManager.upBatteryLevel()
    }

    private static func isSimulator() -> Bool {
        #if targetEnvironment(simulator)
        return true
        #else
        return false
        #endif
    }

    private static func upBatteryState() {
        let state = UIDevice.current.batteryState
        switch state {
        case .unplugged:
            UtilsKt.isBatteryChargingOrNull = false
            break
        case .charging,
             .full:
            UtilsKt.isBatteryChargingOrNull = true
            break
        case .unknown:
            UtilsKt.isBatteryChargingOrNull = isSimulator() ? false : nil
            break
        @unknown default:
            UtilsKt.isBatteryChargingOrNull = isSimulator() ? false : nil
            break
        }
    }

    private static func upBatteryLevel() {
        let level = Int(UIDevice.current.batteryLevel * 100)
        if level < 0 {
            UtilsKt.batteryLevelOrNull = isSimulator() ? 100 : nil
            return
        }
        UtilsKt.batteryLevelOrNull = level.toKotlinInt()
    }
}
