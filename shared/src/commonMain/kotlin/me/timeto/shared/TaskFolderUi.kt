package me.timeto.shared

import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.TaskFolderDb

data class TaskFolderUi(
    val taskFolderDb: TaskFolderDb,
    val activityDb: ActivityDb?,
) {

    val colorRgba: ColorRgba = when {
        taskFolderDb.isToday -> Palette.orange.dark
        taskFolderDb.isTomorrow -> Palette.indigo.dark
        taskFolderDb.isSomeday -> Palette.blue.dark
        else -> activityDb?.colorRgba ?: Palette.blue.dark
    }
}
