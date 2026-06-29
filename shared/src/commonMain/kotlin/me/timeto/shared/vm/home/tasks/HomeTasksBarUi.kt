package me.timeto.shared.vm.home.tasks

import me.timeto.shared.Cache
import me.timeto.shared.TaskFolderUi
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.vm.task_form.TaskFormStrategy

data class HomeTasksBarUi(
    val taskFolderDb: TaskFolderDb,
    val taskFoldersUi: List<TaskFolderUi>,
) {

    //
    // Add Task

    val addTaskActivityDb: ActivityDb =
        taskFolderDb.selectActivityDbOrNullCached() ?: Cache.activitiesDb.first { it.isOther }

    val addTaskStrategy = TaskFormStrategy.NewTask(
        activityDb = addTaskActivityDb,
        taskFolderDb = taskFolderDb,
    )
}