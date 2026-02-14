import UIKit

let H_PADDING = 16.0
let H_PADDING_HALF = H_PADDING / 2

private let scale: CGFloat = UIScreen.main.scale
let onePx: CGFloat = 1 / scale
let halfDpFloor: CGFloat = CGFloat(Int(scale / 2)) / scale // -=
let halfDpCeil: CGFloat = 1 - halfDpFloor // +=
