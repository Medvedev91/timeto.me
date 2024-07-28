package me.timeto.shared.models

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
        return sortedBy {
            it.tf.timeData?.unixTime?.time ?: Int.MIN_VALUE
        }

    return sortedByDescending {
        it.taskDb.id
    }
}
