import SwiftUI
import shared

struct FormButtonSymbolView: View {
    
    let symbol: Symbol
    let color: Color
    
    var body: some View {
        SymbolView(
            symbol: symbol,
            color: color,
            letterSize: 22,
            iconSize: 18,
            emojiSize: 22,
        )
    }
}
