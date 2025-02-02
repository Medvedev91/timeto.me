import SwiftUI

let roundedShape = RoundedRectangle(cornerRadius: 99, style: .circular)
let squircleShape = RoundedRectangle(cornerRadius: 12, style: .continuous)

func buildTimerFont(size: CGFloat) -> Font {
    Font.custom("NotoSansMono-ExtraBold", size: size)
}
