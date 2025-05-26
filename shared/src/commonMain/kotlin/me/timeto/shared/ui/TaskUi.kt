package me.timeto.shared.ui

import me.timeto.shared.TextFeatures
import me.timeto.shared.db.TaskDb
import me.timeto.shared.textFeatures

data class TaskUi(
    val taskDb: TaskDb,
) {

    val tf: TextFeatures = taskDb.text.textFeatures()
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
