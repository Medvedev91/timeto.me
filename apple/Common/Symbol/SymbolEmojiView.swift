import SwiftUI
import shared

struct SymbolEmojiView: View {
    
    let emoji: Symbol.Emoji
    let size: CGFloat
    
    var body: some View {
        Text(emoji.emoji)
            .font(.system(size: size, weight: .semibold))
    }
}
