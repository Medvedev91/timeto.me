package timeto.shared.ui

import timeto.shared.TextFeatures
import timeto.shared.db.TaskFolderModel
import timeto.shared.db.TaskModel

abstract class TaskUI(
    val task: TaskModel
) {
    val textFeatures = TextFeatures.parse(task.text)
    val listText = textFeatures.textUI()
}

///
/// GD Tasks Sorting

fun <T : TaskUI> List<T>.sortedByFolder(
    folder: TaskFolderModel,
): List<T> {
    if (!folder.isToday)
        return this.sortedByDescending { it.task.id }

    return this
        .groupBy { taskUI ->
            taskUI.textFeatures.fromRepeating?.day
                ?: taskUI.textFeatures.timeUI?.unixTime?.localDay
                ?: taskUI.task.unixTime().localDay
        }
        .toList()
        .sortedByDescending { it.first }
        .map { it.second.sortedInsideDay() }
        .flatten()
}

private fun <T : TaskUI> List<T>.sortedInsideDay(): List<T> {
    val (tasksWithDaytime, tasksNoDaytime) = this.partition { it.textFeatures.timeUI != null }

    val resList = mutableListOf<T>()
    tasksNoDaytime
        .sortedByDescending { it.task.id }
        .forEach { resList.add(it) }
    tasksWithDaytime
        .sortedBy { it.textFeatures.timeUI!!.unixTime.time }
        .forEach { resList.add(it) }

    return resList
}

//////
