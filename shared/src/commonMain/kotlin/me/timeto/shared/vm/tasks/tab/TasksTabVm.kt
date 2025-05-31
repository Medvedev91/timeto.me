package me.timeto.shared.vm.tasks.tab

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.onEachExIn
import me.timeto.shared.vm.__Vm

class TasksTabVm : __Vm<TasksTabVm.State>() {

    data class State(
        val taskFoldersUi: List<TaskFolderUi>,
        val initFolder: TaskFolderDb,
    )

    override val state = MutableStateFlow(
        State(
            taskFoldersUi =
                Cache.taskFoldersDbSorted.map { TaskFolderUi(it) },
            initFolder = Cache.getTodayFolderDb(),
        )
    )

    init {
        val scopeVm = scopeVm()
        TaskFolderDb.selectAllSortedFlow().onEachExIn(scopeVm) { folders ->
            state.update {
                it.copy(taskFoldersUi = folders.map { TaskFolderUi(it) })
            }
        }
    }

    ///

    data class TaskFolderUi(
        val taskFolderDb: TaskFolderDb,
    ) {
        val tabText: String =
            taskFolderDb.name.toTabText()
    }
}

private fun String.toTabText(): String =
    this.uppercase().split("").joinToString("\n").trim()
