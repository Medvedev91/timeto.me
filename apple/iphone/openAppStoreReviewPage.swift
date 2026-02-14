import SwiftUI

func openAppStoreReviewPage() {
    let appStoreUrl = "https://itunes.apple.com/app/id6448869727?action=write-review"
    guard let reviewUrl = URL(string: appStoreUrl) else {
        return
    }
    UIApplication.shared.open(reviewUrl, options: [:], completionHandler: nil)
}
