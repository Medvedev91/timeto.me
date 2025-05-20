import SwiftUI
import shared

class TasksTabDropItem: ObservableObject {

    let name: String
    let square: Square

    init(name: String, square: Square) {
        self.name = name
        self.square = square
    }

    class Square {

        var x1: CGFloat
        var y1: CGFloat
        var x2: CGFloat
        var y2: CGFloat

        init(x1: CGFloat = 0, y1: CGFloat = 0, x2: CGFloat = 0, y2: CGFloat = 0) {
            self.x1 = x1
            self.y1 = y1
            self.x2 = x2
            self.y2 = y2
        }

        func upByRect(rect: CGRect) {
            x1 = rect.origin.x
            y1 = rect.origin.y
            x2 = rect.origin.x + rect.width
            y2 = rect.origin.y + rect.height
        }
    }
}

///

class TasksTabDropItemCalendar: TasksTabDropItem {

    init() {
        super.init(name: "Calendar", square: TasksTabDropItem.Square())
    }
}

class TasksTabDropItemTaskFolder: TasksTabDropItem {

    let taskFolderDb: TaskFolderDb

    init(_ taskFolderDb: TaskFolderDb) {
        self.taskFolderDb = taskFolderDb
        super.init(name: taskFolderDb.name, square: TasksTabDropItem.Square())
    }
}
