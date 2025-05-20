import shared

enum TasksTabSectionEnum {
    
    case taskFolder(taskFolderDb: TaskFolderDb)
    
    case repeatings
    
    case calendar
}
