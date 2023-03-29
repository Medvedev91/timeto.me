package timeto.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timeto.shared.db.TaskFolderModel
import timeto.shared.db.TaskFolderModel.Companion.sortedFolders
import timeto.shared.db.TaskModel
import timeto.shared.vm.__VM
import timeto.shared.vm.ui.sortedByFolder

class WatchTabTasksVM : __VM<WatchTabTasksVM.State>() {

    class TaskUI(
        val task: TaskModel,
    ) {

        fun start(
            onStarted: () -> Unit,
            needSheet: () -> Unit, // todo data for sheet
        ) {
            val autostartData = taskAutostartData(task) ?: return needSheet()
            launchExDefault {
                WatchToIosSync.startTaskWithLocal(
                    activity = autostartData.first,
                    deadline = autostartData.second,
                    task = task
                )
                onStarted()
            }
        }
    }

    data class FolderUI(
        val title: String,
        val tasks: List<TaskUI>,
    )

    data class State(
        val foldersUI: List<FolderUI>
    )

    override val state = MutableStateFlow(
        State(
            foldersUI = listOf()
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        TaskModel.getAscFlow().onEachExIn(scope) { tasks ->
            upFolders(tasks)
        }
        TaskFolderModel.anyChangeFlow().onEachExIn(scope) {
            upFolders(TaskModel.getAsc())
        }
    }

    private suspend fun upFolders(allTasks: List<TaskModel>) {
        val foldersUI = TaskFolderModel.getAscBySort().sortedFolders().map { folder ->
            FolderUI(
                title = folder.name,
                tasks = allTasks
                    .filter { it.folder_id == folder.id }
                    .sortedByFolder(folder)
                    .map { TaskUI(it) }
            )
        }
        state.update { it.copy(foldersUI = foldersUI) }
    }
}
