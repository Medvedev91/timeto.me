import SwiftUI
import shared

extension ColorRgba {
    
    func toColor() -> Color {
        Color(rgba: [r.toInt(), g.toInt(), b.toInt(), a.toInt()])
    }
}
