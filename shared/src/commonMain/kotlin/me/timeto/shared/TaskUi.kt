package me.timeto.shared

import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.db.TaskFolderDb

data class TaskUi(
    val taskDb: TaskDb,
) {

    val tf: TextFeatures =
        taskDb.text.textFeatures()

    val taskFolderDb: TaskFolderDb =
        taskDb.selectTaskFolderDbCached()

    fun updateTaskFolder(taskFolderDb: TaskFolderDb) {
        launchExIo {
            taskDb.updateFolder(
                taskFolderDb = taskFolderDb,
                updateFolderActivity = true,
                replaceIfTmrw = true,
            )
        }
    }

    fun moveToTimer() {
        launchExIo {
            val taskText: String =
                taskDb.text.textFeatures().textNoFeatures
            if (taskText.isEmpty()) {
                taskDb.delete()
                return@launchExIo
            }
            val intervalDb: IntervalDb =
                IntervalDb.selectLastOneOrNull()!!
            val intervalTf: TextFeatures =
                (intervalDb.note ?: "").textFeatures()
            val intervalText: String =
                intervalTf.textNoFeatures
            val newTf: TextFeatures = intervalTf.copy(
                textNoFeatures =
                    if (intervalText.isBlank()) taskText
                    else "$intervalText\n$taskText"
            )
            intervalDb.updateNote(newTf.textWithFeatures())
            taskDb.delete()
        }
    }

    fun delete() {
        launchExIo {
            taskDb.delete()
        }
    }
}

fun List<TaskUi>.sortedUi(
    isToday: Boolean,
): List<TaskUi> {

    if (isToday)
        return sortedWith(
            compareBy<TaskUi> { it.tf.calcTimeData()?.unixTime?.time ?: 0 }
                .thenByDescending { it.taskDb.id }
        )

    return sortedByDescending {
        it.taskDb.id
    }
}
