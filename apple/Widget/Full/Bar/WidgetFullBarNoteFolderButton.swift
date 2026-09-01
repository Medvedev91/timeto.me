import SwiftUI
import shared

struct WidgetFullBarNoteFolderButton: View {
    
    let noteFolderUi: NoteFolderUi
    let color: Color
    
    var body: some View {
        WidgetFullBarIconButton(
            widgetLink: WidgetLink.NoteFolder(noteFolderId: noteFolderUi.noteFolderDb.id.toInt()),
            content: {
                SymbolView(
                    symbol: noteFolderUi.symbol,
                    color: color,
                    letterSize: widgetFullBarLetterSize,
                    iconSize: widgetFullBarIconSize,
                    emojiSize: widgetFullBarLetterSize,
                )
            }
        )
    }
}
