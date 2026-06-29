import SwiftUI
import shared

struct HomeTasksBarView: View {
    
    let tasksBarUi: HomeTasksBarUi
    let changeTaskFolder: (TaskFolderUi) -> Void
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        HStack {
            HStack {
                Text("Task..")
                    .font(.system(size: HomeScreen__primaryFontSize))
                    .foregroundColor(.secondary)
                    .lineLimit(1)
                    .padding(.trailing, 8)
                Spacer()
            }
            .fillMaxHeight()
            .background(.black) // Clickable area
            .padding(.trailing, 8)
            .onTapGesture {
                navigation.showTaskForm(strategy: tasksBarUi.addTaskStrategy)
            }
            .padding(.leading, homeTasksInnerHPadding)
            
            ForEach(tasksBarUi.taskFoldersUi, id: \.self) { taskFolderUi in
                HomeTasksFolderButton(
                    taskFolderUi: taskFolderUi,
                    color: taskFolderUi.taskFolderDb.id != tasksBarUi.taskFolderDb.id ? Color(.systemGray2) : taskFolderUi.colorRgba.toColor(),
                    onClick: {
                        changeTaskFolder(taskFolderUi)
                    },
                )
            }
            
            HomeTasksCalendarButton(
                color: Color(.systemGray2),
                onClick: {
                    navigation.fullScreen {
                        CalendarTabsView()
                    }
                },
            )
        }
        .frame(height: HomeScreen__itemHeight)
        .fillMaxWidth()
        .padding(.leading, homeTasksOuterHPadding)
    }
}
