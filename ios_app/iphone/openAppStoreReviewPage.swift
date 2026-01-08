import SwiftUI

func openAppStoreReviewPage() {
    let appStoreURL = "https://itunes.apple.com/app/id6448869727?action=write-review"
    guard let reviewURL = URL(string: appStoreURL) else {
        return
    }
    UIApplication.shared.open(reviewURL, options: [:], completionHandler: nil)
}
