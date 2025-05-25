import SwiftUI
import Combine
import WatchConnectivity
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

// todo remove
func sinDegrees(_ degrees: Double) -> Double {
    sin(degrees * Double.pi / 180.0)
}

// todo remove
func cosDegrees(_ degrees: Double) -> Double {
    cos(degrees * Double.pi / 180.0)
}

struct c {

    static let red = ColorRgba.companion.red.toColor()
    static let green = ColorRgba.companion.green.toColor()
    static let blue = ColorRgba.companion.blue.toColor()
    static let orange = ColorRgba.companion.orange.toColor()
    static let purple = ColorRgba.companion.purple.toColor()

    static let text = ColorRgba.companion.text.toColor()
    static let textSecondary = ColorRgba.companion.textSecondary.toColor()
    static let tertiaryText = ColorRgba.companion.tertiaryText.toColor()

    static let bg = ColorRgba.companion.bg.toColor()
    static let fg = ColorRgba.companion.fg.toColor()

    static let dividerBg = ColorRgba.companion.dividerBg.toColor()
    static let dividerFg = ColorRgba.companion.dividerFg.toColor()

    static let sheetBg = ColorRgba.companion.sheetBg.toColor()
    static let sheetFg = ColorRgba.companion.sheetFg.toColor()

    static let homeFontSecondary = ColorRgba.companion.homeFontSecondary.toColor()
    static let homeMenuTime = ColorRgba.companion.homeMenuTime.toColor()
    static let homeFg = ColorRgba.companion.homeFg.toColor()

    static let tasksDropFocused = ColorRgba.companion.tasksDropFocused.toColor()
}
