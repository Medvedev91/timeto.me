package me.timeto.shared.models

import me.timeto.shared.TextFeatures
import me.timeto.shared.db.TaskDb
import me.timeto.shared.textFeatures

data class TaskUi(
    val taskDb: TaskDb,
) {

    val taskTf: TextFeatures = taskDb.text.textFeatures()
}
