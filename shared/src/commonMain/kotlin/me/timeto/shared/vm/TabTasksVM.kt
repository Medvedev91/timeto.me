package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.DI
import me.timeto.shared.db.TaskFolderModel
import me.timeto.shared.db.TaskFolderModel.Companion.sortedFolders
import me.timeto.shared.onEachExIn

class TabTasksVM : __VM<TabTasksVM.State>() {

    data class State(
        val folders: List<TaskFolderModel>,
    )

    override val state = MutableStateFlow(
        State(
            folders = DI.taskFolders.sortedFolders()
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        TaskFolderModel.getAscBySortFlow().onEachExIn(scope) { folders ->
            state.update { it.copy(folders = folders.sortedFolders()) }
        }
    }
}
