import SwiftUI
import Combine
import WatchConnectivity
import shared

// todo remove
let DEF_LIST_H_PADDING = 16.0
let DEF_LIST_V_PADDING = 12.0

let onePx = 1 / UIScreen.main.scale

func hideKeyboard() {
    UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
}

func schedulePush(data: ScheduledNotificationData) {
    let content = UNMutableNotificationContent()
    content.title = data.title
    content.body = data.text

    if data.type == .break_ {
        let soundFile = UtilsKt.getSoundTimeToBreakFileName(withExtension: true)
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

/// Sheet uses default colors. Set by hardcode.
extension UIColor {

    private static let tgLikeLightBg: UInt = 0xFFEFEFF3

    //////

    static func myDayNight(
            _ light: UIColor,
            _ dark: UIColor
    ) -> UIColor {
        UIColor { $0.userInterfaceStyle == .light ? light : dark }
    }

    static func myDayNightArgb(
            _ light: UInt,
            _ dark: UInt
    ) -> UIColor {
        myDayNight(UIColor(argb: light), UIColor(argb: dark))
    }

    //////

    static var bg: UIColor = myDayNight(.white, .black)

    static var timerBarBackground: UIColor = myDayNightArgb(0xFFE3F2FD, 0xFF1C1C1E)

    // todo remove
    static var myBackground: UIColor = myDayNight(UIColor(argb: tgLikeLightBg), .black)

    // todo remove
    static var mySecondaryBackground = myDayNightArgb(0xFFFFFFFF, 0xFF1C1C1E)

    static var mySheetFormBg = myDayNightArgb(tgLikeLightBg, 0xFF121214)

    static var myFormButtonRightNoteText = myDayNightArgb(0x88000000, 0x88FFFFFF)

    static var iconButtonBg1 = myDayNightArgb(0xFFAEAEB2, 0xFF636366)

    static var dividerBg2 = UIColor.systemGray4

    static var formHeaderDivider = UIColor.systemGray3

    static var formHeaderBackground = myDayNightArgb(0xFFF9F9F9, 0xFF191919)
}

extension View {

    //
    // https://stackoverflow.com/a/56558187
    //
    // Different behavior of text blocks. On emulator often
    // only one line is displayed, on the device it is ok.
    //
    func myMultilineText() -> some View {
        fixedSize(horizontal: false, vertical: true)
    }
}


///
/// In app notification
/// https://youtu.be/LSU-QmeUXP0?t=176
///
class MyInAppNotificationDelegate: NSObject, ObservableObject, UNUserNotificationCenterDelegate {

    ///
    /// Called if the notification comes at a time when the app is open - in app.
    ///
    func userNotificationCenter(
            _ center: UNUserNotificationCenter,
            willPresent notification: UNNotification,
            withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // Haptic vibration
        UINotificationFeedbackGenerator().notificationOccurred(.success)

        /// https://developer.apple.com/documentation/usernotifications/unnotificationpresentationoptions
        /// Without .banner there's nothing, not even a sound. Banner is the usual appearance on top.
        // .sound
        // .list /// Keep in notification center
        // .badge /// Set badge on app icon
        completionHandler(FullScreenUI.shared.isOpen() ? [.banner, .sound] : [.banner])
    }

    ///
    /// On click on push
    ///
    func userNotificationCenter(
            _ center: UNUserNotificationCenter,
            didReceive response: UNNotificationResponse,
            withCompletionHandler completionHandler: @escaping () -> ()
    ) {
        TabsView.lastInstance?.tabSelection = TabsView.TAB_ID_TIMER
        completionHandler() // todo What is it? What if remove.
    }
}

///
/// https://stackoverflow.com/a/66880368/5169420

private struct SafeAreaInsetsKey: EnvironmentKey {
    static var defaultValue: EdgeInsets {
        (UIApplication.shared.windows.first(where: { $0.isKeyWindow })?.safeAreaInsets ?? .zero).insets
    }
}

extension ColorRgba {
    func toColor() -> Color {
        Color(rgba: [r.toInt(), g.toInt(), b.toInt(), a.toInt()])
    }
}


extension EnvironmentValues {
    var safeAreaInsets: EdgeInsets {
        self[SafeAreaInsetsKey.self]
    }
}

private extension UIEdgeInsets {
    var insets: EdgeInsets {
        EdgeInsets(top: top, leading: left, bottom: bottom, trailing: right)
    }
}

//////

func vibrate(_ type: UINotificationFeedbackGenerator.FeedbackType) {
    UINotificationFeedbackGenerator().notificationOccurred(type)
}

extension View {

    func presentationDetentsHeightIf16(
            _ height: CGFloat,
            withDragIndicator: Bool = true
    ) -> some View {
        if #available(iOS 16.0, *) {
            return self
                    .presentationDetents([.height(height)])
                    .presentationDragIndicator(withDragIndicator ? .visible : .hidden)
        }
        return self
    }

    func presentationDetentsMediumIf16(
            withDragIndicator: Bool = true
    ) -> some View {
        if #available(iOS 16.0, *) {
            return self
                    .presentationDetents([.medium])
                    .presentationDragIndicator(withDragIndicator ? .visible : .hidden)
        }
        return self
    }
}

///
/// Rounded corners https://stackoverflow.com/a/58606176/5169420

private struct RoundedCorner: Shape {

    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(roundedRect: rect, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}

///
///

private struct SafeAreaPaddingsModifier: ViewModifier {

    @Environment(\.safeAreaInsets) private var safeAreaInsets

    let edges: Edge.Set

    func body(content: Content) -> some View {
        content
                .padding(.top, edges.contains(.top) ? safeAreaInsets.top : 0)
                .padding(.bottom, edges.contains(.bottom) ? safeAreaInsets.bottom : 0)
    }
}

extension View {

    func safeAreaPadding(_ edges: Edge.Set) -> some View {
        modifier(SafeAreaPaddingsModifier(edges: edges))
    }
}
