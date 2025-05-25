import SwiftUI
import shared

extension ColorRgba {
    
    func toColor() -> Color {
        Color(r: r.toInt(), g: g.toInt(), b: b.toInt(), a: a.toInt())
    }
}
