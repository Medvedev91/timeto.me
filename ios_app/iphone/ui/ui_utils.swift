import SwiftUI
import shared

let H_PADDING = 16.0
let H_PADDING_HALF = H_PADDING / 2

private let scale: CGFloat = UIScreen.main.scale
let onePx: CGFloat = 1 / scale
let halfDpFloor: CGFloat = CGFloat(Int(scale / 2)) / scale // -=
let halfDpCeil: CGFloat = 1 - halfDpFloor // +=

func hideKeyboard() {
    UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
}

func showOpenSource() {
    UIApplication.shared.open(URL(string: Utils_kmpKt.OPEN_SOURCE_URL)!)
}
