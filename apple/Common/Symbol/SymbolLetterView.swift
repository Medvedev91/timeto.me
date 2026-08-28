import SwiftUI
import shared

struct SymbolLetterView: View {
    
    let letter: Symbol.Letter
    let color: Color
    let size: CGFloat
    
    var body: some View {
        Text(letter.letter)
            .font(.system(size: size, weight: .semibold))
            .foregroundColor(color)
    }
}
