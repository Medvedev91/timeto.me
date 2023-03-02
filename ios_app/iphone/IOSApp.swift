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

    init() {
        UtilsPlatformKt.doInitKmmIos(deviceName: machineIdentifier())
    }

    var body: some Scene {

        WindowGroup {

            VMView(vm: vm) { state in

                if state.isAppReady {

                    TabsView()
                            .attachTimetoSheet()
                            .attachTimetoAlert()
                            .attachAutoBackup()
                            .attachTimerFullScreenView()
                            .onReceive(scheduledNotificationsDataPublisher) {
                                let center = UNUserNotificationCenter.current()
                                center.removeAllPendingNotificationRequests()
                                let dataItems = $0 as! [ScheduledNotificationData]
                                dataItems.forEach { data in schedulePush(data: data) }
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
