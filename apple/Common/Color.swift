import SwiftUI

extension Color {

    init(r: Int, g: Int, b: Int, a: Int) {
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}
