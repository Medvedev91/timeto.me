package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.DI
import me.timeto.shared.db.TaskFolderModel
import me.timeto.shared.db.TaskFolderModel.Companion.sortedFolders
import me.timeto.shared.onEachExIn

class TabTasksVM : __VM<TabTasksVM.State>() {

    data class TaskFolderUI(
        val folder: TaskFolderModel,
    ) {
        val tabText = folder.name.uppercase().split("").joinToString("\n").trim()
    }

    data class State(
        val taskFoldersUI: List<TaskFolderUI>,
    )

    override val state = MutableStateFlow(
        State(
            taskFoldersUI = DI.taskFolders.sortedFolders().map { TaskFolderUI(it) },
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        TaskFolderModel.getAscBySortFlow().onEachExIn(scope) { folders ->
            val taskFoldersUI = folders.sortedFolders().map { TaskFolderUI(it) }
            state.update { it.copy(taskFoldersUI = taskFoldersUI) }
        }
    }
}
