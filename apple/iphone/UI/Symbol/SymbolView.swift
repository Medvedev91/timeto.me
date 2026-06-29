import SwiftUI
import shared

struct SymbolView: View {
    
    let symbol: Symbol
    let color: Color
    let letterSize: CGFloat
    let iconSize: CGFloat
    let emojiSize: CGFloat
    
    var body: some View {
        if let letter = symbol as? Symbol.Letter {
            SymbolLetterView(
                letter: letter,
                color: color,
                size: letterSize,
            )
        } else if let icon = symbol as? Symbol.Icon {
            SymbolIconView(
                icon: icon,
                color: color,
                size: iconSize,
            )
        } else if let emoji = symbol as? Symbol.Emoji {
            SymbolEmojiView(
                emoji: emoji,
                size: emojiSize,
            )
        } else {
            // todo handle sealed like enum
        }
    }
}
