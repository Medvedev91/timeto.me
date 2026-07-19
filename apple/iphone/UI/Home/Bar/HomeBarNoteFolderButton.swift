import SwiftUI
import shared

struct HomeBarNoteFolderButton: View {
    
    let noteFolderUi: NoteFolderUi
    let color: Color
    let onClick: () -> Void
    
    var body: some View {
        HomeBarIconButton(
            onClick: {
                onClick()
            },
            content: {
                SymbolView(
                    symbol: noteFolderUi.symbol,
                    color: color,
                    letterSize: homeBarLetterSize,
                    iconSize: homeBarIconSize,
                    emojiSize: homeBarLetterSize,
                )
            }
        )
    }
}
