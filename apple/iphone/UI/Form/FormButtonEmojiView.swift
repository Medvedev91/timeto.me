import SwiftUI

struct FormButtonEmojiView: View {
    
    let emoji: String
    
    var body: some View {
        Text(emoji)
            .font(.system(size: 22))
    }
}
