package me.timeto.shared.vm.tasks.form

import me.timeto.shared.db.TaskDb
import me.timeto.shared.db.TaskFolderDb

sealed class TaskFormStrategy {

    class NewTask(
        val taskFolderDb: TaskFolderDb,
    ) : TaskFormStrategy()

    class EditTask(
        val taskDb: TaskDb,
    ) : TaskFormStrategy()
}
