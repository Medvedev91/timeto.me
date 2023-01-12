import UIKit
import Combine

///
/// https://betterprogramming.pub/observing-keyboard-changes-in-swiftui-96dd06a2b952
/// But the closing didn't work. Fixed by "c2" implementation
///
class KeyboardManager: ObservableObject {

    @Published var height = 0.0

    private var c1: Cancellable?
    private var c2: Cancellable?

    init() {

        c1 = NotificationCenter.default
                .publisher(for: UIWindow.keyboardWillShowNotification)
                .sink { [weak self] notification in

                    guard let self = self else {
                        return
                    }

                    guard let userInfo = notification.userInfo else {
                        return
                    }
                    guard let keyboardFrame = userInfo[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect else {
                        return
                    }

                    self.height = keyboardFrame.height
                }

        c2 = NotificationCenter.default
                .publisher(for: UIWindow.keyboardWillHideNotification)
                .sink { [weak self] notification in
                    guard let self = self else {
                        return
                    }
                    self.height = 0
                }
    }
}
