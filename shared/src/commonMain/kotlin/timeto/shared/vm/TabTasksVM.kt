package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.DI
import timeto.shared.db.TaskFolderModel
import timeto.shared.db.TaskFolderModel.Companion.sortedFolders
import timeto.shared.onEachExIn

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
