import SwiftUI
import shared

struct WidgetFullBarView: View {
    
    let homeBarUi: HomeBarUi
    
    ///
    
    var body: some View {
        HStack {
            Link(destination: WidgetLink.NewTask.buildUrl()) {
                
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
                        .font(.system(size: widgetFullFontSize))
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                        .padding(.trailing, 8)
                    Spacer()
                }
                .fillMaxHeight()
                .background(.black) // Clickable area
                .padding(.trailing, 8)
            }
            
            ForEach(homeBarUi.taskFoldersUi, id: \.self) { taskFolderUi in
                let activeFolderId: Int32? = (homeBarUi.homeMode as? HomeMode.TaskFolder)?.taskFolderDb.id
                WidgetFullBarTaskFolderButton(
                    taskFolderUi: taskFolderUi,
                    color: taskFolderUi.taskFolderDb.id != activeFolderId ? Color(.systemGray2) : taskFolderUi.colorRgba.toColor(),
                )
            }
            
            ForEach(homeBarUi.noteFoldersUi, id: \.self) { noteFolderUi in
                let activeFolderId: Int32? = (homeBarUi.homeMode as? HomeMode.NoteFolder)?.noteFolderDb.id
                WidgetFullBarNoteFolderButton(
                    noteFolderUi: noteFolderUi,
                    color: noteFolderUi.noteFolderDb.id != activeFolderId ? Color(.systemGray2) : .blue,
                )
            }
            
            WidgetFullBarCalendarButton(
                color: Color(.systemGray2),
            )
        }
        .frame(height: widgetFullItemHeight)
        .padding(.leading, widgetFullHPadding)
        .padding(.trailing, 4)
        .fillMaxWidth()
    }
}
