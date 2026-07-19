import SwiftUI
import shared

struct HomeBarView: View {
    
    let homeBarUi: HomeBarUi
    let changeTaskFolder: (TaskFolderUi) -> Void
    let changeNoteFolder: (NoteFolderUi) -> Void
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        HStack {
            HStack {
                let placeholder: String = {
                    if homeBarUi.homeMode is HomeMode.TaskFolder {
                        return "Task.."
                    }
                    else if homeBarUi.homeMode is HomeMode.NoteFolder {
                        return "Note.."
                    }
                    else {
                        fatalError()
                    }
                }()
                Text(placeholder)
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
                if let homeMode = homeBarUi.homeMode as? HomeMode.TaskFolder {
                    navigation.showTaskForm(strategy: homeMode.addTaskStrategy)
                }
                else if let homeMode = homeBarUi.homeMode as? HomeMode.NoteFolder {
                    navigation.sheet {
                        NoteFormSheet(
                            noteFormLogic: homeMode.addNoteLogic,
                            onDelete: {},
                        )
                    }
                }
                else {
                    fatalError()
                }
            }
            .padding(.leading, homeTasksInnerHPadding)
            
            ForEach(homeBarUi.taskFoldersUi, id: \.self) { taskFolderUi in
                let activeFolderId: Int32? = (homeBarUi.homeMode as? HomeMode.TaskFolder)?.taskFolderDb.id
                HomeBarTaskFolderButton(
                    taskFolderUi: taskFolderUi,
                    color: taskFolderUi.taskFolderDb.id != activeFolderId ? Color(.systemGray2) : taskFolderUi.colorRgba.toColor(),
                    onClick: {
                        changeTaskFolder(taskFolderUi)
                    },
                )
            }
            
            ForEach(homeBarUi.noteFoldersUi, id: \.self) { noteFolderUi in
                let activeFolderId: Int32? = (homeBarUi.homeMode as? HomeMode.NoteFolder)?.noteFolderDb.id
                HomeBarNoteFolderButton(
                    noteFolderUi: noteFolderUi,
                    color: noteFolderUi.noteFolderDb.id != activeFolderId ? Color(.systemGray2) : .blue,
                    onClick: {
                        changeNoteFolder(noteFolderUi)
                    },
                )
            }
            
            HomeBarCalendarButton(
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
