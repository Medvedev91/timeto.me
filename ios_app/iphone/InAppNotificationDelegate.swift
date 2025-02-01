import SwiftUI

class InAppNotificationDelegate: NSObject, ObservableObject, UNUserNotificationCenterDelegate {
    
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // Haptic vibration
        UINotificationFeedbackGenerator().notificationOccurred(.success)
        
        // https://developer.apple.com/documentation/usernotifications/unnotificationpresentationoptions
        // Without .banner there's nothing, not even a sound. Banner is the usual appearance on top.
        // .sound
        // .list - keep in notification center
        // .badge - set badge on app icon
        completionHandler([.banner, .sound])
    }
}
