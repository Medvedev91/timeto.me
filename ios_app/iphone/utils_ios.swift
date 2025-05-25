import SwiftUI
import shared

func schedulePush(data: ScheduledNotificationData) {
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

struct c {

    static let bg = ColorRgba.companion.bg.toColor()
    static let fg = ColorRgba.companion.fg.toColor()

    static let dividerBg = ColorRgba.companion.dividerBg.toColor()
    static let dividerFg = ColorRgba.companion.dividerFg.toColor()

    static let sheetFg = ColorRgba.companion.sheetFg.toColor()

    static let homeFontSecondary = ColorRgba.companion.homeFontSecondary.toColor()
    static let homeMenuTime = ColorRgba.companion.homeMenuTime.toColor()
    static let homeFg = ColorRgba.companion.homeFg.toColor()

    static let tasksDropFocused = ColorRgba.companion.tasksDropFocused.toColor()
}
