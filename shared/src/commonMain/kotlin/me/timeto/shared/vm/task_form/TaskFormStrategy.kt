package me.timeto.shared.vm.task_form

import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.db.TaskFolderDb

sealed class TaskFormStrategy {

    class NewTask(
        val activityDb: ActivityDb,
        val taskFolderDb: TaskFolderDb,
    ) : TaskFormStrategy()

    class EditTask(
        val taskDb: TaskDb,
    ) : TaskFormStrategy()
}
