import SwiftUI
import Combine
import shared

private let shortcutPublisher: AnyPublisher<ShortcutDb, Never> =
ShortcutPerformer.shared.flow.toPublisher()

private let scheduledNotificationsDataPublisher: AnyPublisher<NSArray, Never> =
NotificationAlarm.companion.flow.toPublisher()

private let keepScreenOnDataPublisher: AnyPublisher<KotlinBoolean, Never> =
KeepScreenOnKt.keepScreenOnStateFlow.toPublisher()

struct MainScreen: View {
    
    @State private var tab: MainTabEnum = .home
    
    @State private var isShortcutErrorPresented: Bool = false
    
    var body: some View {
        
        ZStack(alignment: .bottom) {
            
            ActivityScreen(tab: $tab)
                .attachNavigation()
                .zIndex(tab == .activity ? 1 : 0)

            HomeScreen()
                .attachNavigation()
                .zIndex(tab == .home ? 1 : 0)

            TasksTabView()
                .attachNavigation()
                .zIndex(tab == .tasks ? 1 : 0)

            SettingsScreen(tab: $tab)
                .attachNavigation()
                .zIndex(tab == .settings ? 1 : 0)

            MainTabsView(tab: $tab)
                .zIndex(2)
        }
        .ignoresSafeArea(.keyboard) // Hide tab bar under the keyboard
        .onReceive(shortcutPublisher) { shortcutDb in
            guard let swiftUrl = URL(string: shortcutDb.uri), UIApplication.shared.canOpenURL(swiftUrl) else {
                isShortcutErrorPresented = true
                return
            }
            UIApplication.shared.open(swiftUrl)
        }
        .onReceive(scheduledNotificationsDataPublisher) {
            let center = UNUserNotificationCenter.current()
            center.removeAllPendingNotificationRequests()
            let alarms = $0 as! [NotificationAlarm]
            alarms.forEach { alarm in
                schedulePush(notificationAlarm: alarm)
            }
        }
        .onReceive(keepScreenOnDataPublisher) { keepScreenOn in
            UIApplication.shared.isIdleTimerDisabled = (keepScreenOn == true)
        }
        .alert(
            "Invalid shortcut link",
            isPresented: $isShortcutErrorPresented,
            actions: {
                Button("Ok", role: .cancel) {
                }
            }
        )
    }
}

private func schedulePush(
    notificationAlarm: NotificationAlarm
) {
    let content = UNMutableNotificationContent()
    content.title = notificationAlarm.title
    content.body = notificationAlarm.text

    if notificationAlarm.type == .timetobreak {
        let soundFile = GetSoundTimerExpiredFileNameKt.getSoundTimerExpiredFileName(withExtension: true)
        content.sound = UNNotificationSound(named: UNNotificationSoundName(rawValue: soundFile))
    } else {
        content.sound = .default
    }
    content.badge = 1

    let trigger = UNTimeIntervalNotificationTrigger(
        timeInterval: TimeInterval(notificationAlarm.inSeconds.toInt()),
        repeats: false
    )

    let req = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: trigger)

    let center = UNUserNotificationCenter.current()
    center.add(req, withCompletionHandler: nil)
}
