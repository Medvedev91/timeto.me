import SwiftUI
import Combine
import shared

private let shortcutPublisher: AnyPublisher<ShortcutDb, Never> =
ShortcutPerformer.shared.flow.toPublisher()

private let scheduledNotificationsDataPublisher: AnyPublisher<NSArray, Never> =
Utils_kmpKt.scheduledNotificationsDataFlow.toPublisher()

private let keepScreenOnDataPublisher: AnyPublisher<KotlinBoolean, Never> =
Utils_kmpKt.keepScreenOnStateFlow.toPublisher()

struct MainScreen: View {
    
    @State private var tab: MainTabEnum = .home
    
    @State private var isShortcutErrorPresented: Bool = false
    
    var body: some View {
        
        ZStack(alignment: .bottom) {
            
            switch tab {
            case .home:
                HomeScreen()
                    .attachNavigation()
            case .activities:
                ActivitiesScreen(tab: $tab)
                    .attachNavigation()
            case .tasks:
                TasksTabView()
                    .attachNavigation()
            case .settings:
                SettingsScreen()
                    .attachNavigation()
            }
            
            MainTabsView(tab: $tab)
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
            let dataItems = $0 as! [ScheduledNotificationData]
            dataItems.forEach { data in
                schedulePush(data: data)
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
    data: ScheduledNotificationData
) {
    let content = UNMutableNotificationContent()
    content.title = data.title
    content.body = data.text

    if data.type == .break_ {
        let soundFile = Utils_kmpKt.getSoundTimerExpiredFileName(withExtension: true)
        content.sound = UNNotificationSound(named: UNNotificationSoundName(rawValue: soundFile))
    } else {
        content.sound = .default
    }
    content.badge = 1

    let trigger = UNTimeIntervalNotificationTrigger(timeInterval: TimeInterval(data.inSeconds.toInt()), repeats: false)

    let req = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: trigger)

    let center = UNUserNotificationCenter.current()
    center.add(req, withCompletionHandler: nil)
}
