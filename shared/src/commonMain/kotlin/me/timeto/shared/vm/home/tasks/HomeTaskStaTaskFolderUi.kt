package me.timeto.shared.vm.home.tasks

import me.timeto.shared.TaskFolderUi
import me.timeto.shared.TaskUi
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.launchExIo

// STA - Swipe to Action
data class HomeTaskStaTaskFolderUi(
    val taskUi: TaskUi,
    val taskFolderUi: TaskFolderUi,
) {

    val taskFolderDb: TaskFolderDb =
        taskFolderUi.taskFolderDb

    val isSelected: Boolean =
        (taskUi.taskDb.folder_id == taskFolderDb.id) ||
                (taskFolderDb.activity_id != null && taskFolderDb.activity_id == taskUi.tf.activityDb?.id)

    fun onTap() {
        launchExIo {
            taskUi.taskDb.updateFolder(
                taskFolderDb = taskFolderDb,
                updateFolderActivity = true,
                replaceIfTmrw = true,
            )
            homeTasksBarFolderAnimateFlow.emit(taskFolderDb.id)
        }
    }
}