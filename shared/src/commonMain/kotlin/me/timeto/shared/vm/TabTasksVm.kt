package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.TaskFolderDb

class TabTasksVm : __Vm<TabTasksVm.State>() {

    data class TaskFolderUI(
        val folder: TaskFolderDb,
    ) {
        val tabText = folder.name.toTabText()
    }

    data class State(
        val taskFoldersUI: List<TaskFolderUI>,
        val initFolder: TaskFolderDb = Cache.getTodayFolderDb(),
    )

    override val state = MutableStateFlow(
        State(
            taskFoldersUI = Cache.taskFoldersDbSorted.map { TaskFolderUI(it) },
        )
    )

    override fun onAppear() {
        val scope = scopeVm()
        TaskFolderDb.selectAllSortedFlow().onEachExIn(scope) { folders ->
            val taskFoldersUI = folders.map { TaskFolderUI(it) }
            state.update { it.copy(taskFoldersUI = taskFoldersUI) }
        }
    }
}

private fun String.toTabText(): String =
    this.uppercase().split("").joinToString("\n").trim()
