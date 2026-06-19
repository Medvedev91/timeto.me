import SwiftUI
import shared

private let iconSize: CGFloat = 19
private let letterSize: CGFloat = 24

struct HomeTasksFolderButton: View {
    
    let taskFolderUi: TaskFolderUi
    let color: Color
    let onClick: () -> Void
    
    var body: some View {
        
        /*
        // todo
        val scaleAnimation = remember { Animatable(1f) }

        // todo
        LaunchedEffect(Unit) {
            homeTasksBarFolderAnimateFlow.collect { folderId ->
                if (folderId == taskFolderUi.taskFolderDb.id) {
                    scaleAnimation.animateTo(1.40f)
                    scaleAnimation.animateTo(1f)
                    scaleAnimation.animateTo(1.25f)
                    scaleAnimation.animateTo(1f)
                }
            }
        }
        */
        
        Button(
            action: {
                onClick()
            },
            label: {
                ZStack {
                    if (taskFolderUi.taskFolderDb.isToday) {
                        Image(systemName: "sun.min.fill")
                            .font(.system(size: iconSize, weight: .semibold))
                            .foregroundColor(color)
                    } else if (taskFolderUi.taskFolderDb.isTomorrow) {
                        Image(systemName: "moon.fill")
                            .font(.system(size: iconSize, weight: .semibold))
                            .foregroundColor(color)
                    } else {
                        SymbolView(
                            symbol: taskFolderUi.symbol,
                            color: color,
                            letterSize: letterSize,
                            iconSize: iconSize,
                            emojiSize: letterSize,
                        )
                    }
                }
            },
        )
        .frame(width: HomeScreen__itemHeight, height: HomeScreen__itemHeight)
    }
}
