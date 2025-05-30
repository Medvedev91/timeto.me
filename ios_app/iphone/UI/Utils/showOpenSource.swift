import UIKit
import shared

func showOpenSource() {
    UIApplication.shared.open(URL(string: OpenSourceUrlKt.openSourceUrl)!)
}
