package me.timeto.shared

import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.TaskDb

fun taskAutostartData(
    task: TaskDb,
): Pair<ActivityDb, Int>? {
    val textFeatures = task.text.textFeatures()
    val activity = textFeatures.activity ?: return null
    val timerTime = textFeatures.timer ?: return null
    return activity to timerTime
}
