package me.timeto.app.ui.tasks.tab

import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import me.timeto.shared.db.TaskFolderDb

sealed class TasksTabDropItem(
    val name: String,
    val square: Square,
) {

    fun upSquareByCoordinates(c: LayoutCoordinates) {
        val p = c.positionInWindow()
        square.x1 = p.x.toInt()
        square.y1 = p.y.toInt()
        square.x2 = p.x.toInt() + c.size.width
        square.y2 = p.y.toInt() + c.size.height
    }

    class Square(var x1: Int, var y1: Int, var x2: Int, var y2: Int)

    //
    // Types

    class Folder(
        val taskFolderDb: TaskFolderDb,
        square: Square,
    ) : TasksTabDropItem(taskFolderDb.name, square)

    class Calendar(
        square: Square,
    ) : TasksTabDropItem("Calendar", square)
}
