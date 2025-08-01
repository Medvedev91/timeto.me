import SwiftUI

func openSystemSettings() {
    UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
}
