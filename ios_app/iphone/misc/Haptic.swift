import SwiftUI

struct Haptic {
    
    static func softShot() {
        shot(.soft)
    }
    
    ///
    
    static func success() {
        notification(.success)
    }
    
    static func warning() {
        notification(.warning)
    }
    
    static func error() {
        notification(.error)
    }
}

///

private func shot(_ style: UIImpactFeedbackGenerator.FeedbackStyle) {
    UIImpactFeedbackGenerator(style: style).impactOccurred()
}

private func notification(_ type: UINotificationFeedbackGenerator.FeedbackType) {
    UINotificationFeedbackGenerator().notificationOccurred(type)
}
