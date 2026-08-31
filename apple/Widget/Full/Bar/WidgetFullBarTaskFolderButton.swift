import SwiftUI
import shared

struct WidgetFullBarTaskFolderButton: View {
    
    let taskFolderUi: TaskFolderUi
    let color: Color
    
    var body: some View {
        
        WidgetFullBarIconButton(
            taskFolderId: taskFolderUi.taskFolderDb.id.toInt(),
            content: {
                ZStack {
                    if (taskFolderUi.taskFolderDb.isToday) {
                        Image(systemName: "sun.min.fill")
                            .font(.system(size: widgetFullBarIconSize, weight: .semibold))
                            .foregroundColor(color)
                    } else if (taskFolderUi.taskFolderDb.isTomorrow) {
                        Image(systemName: "moon.fill")
                            .font(.system(size: widgetFullBarIconSize, weight: .semibold))
                            .foregroundColor(color)
                    } else {
                        SymbolView(
                            symbol: taskFolderUi.symbol,
                            color: color,
                            letterSize: widgetFullBarLetterSize,
                            iconSize: widgetFullBarIconSize,
                            emojiSize: widgetFullBarLetterSize,
                        )
                    }
                }
            },
        )
    }
}
