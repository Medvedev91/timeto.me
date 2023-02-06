package timeto.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timeto.shared.db.TaskFolderModel
import timeto.shared.db.TaskFolderModel.Companion.sortedFolders
import timeto.shared.db.TaskModel
import timeto.shared.ui.sortedByFolder
import timeto.shared.vm.__VM

class WatchTabTasksVM : __VM<WatchTabTasksVM.State>() {

    class TaskUI(
        task: TaskModel,
    ) : timeto.shared.ui.TaskUI(task) {

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
    }

    private suspend fun upFolders(allTasks: List<TaskModel>) {
        val foldersUI = TaskFolderModel.getAscBySort().sortedFolders().map { folder ->
            FolderUI(
                title = folder.name,
                tasks = allTasks
                    .filter { it.folder_id == folder.id }
                    .map { TaskUI(it) }
                    .sortedByFolder(folder)
            )
        }
        state.update { it.copy(foldersUI = foldersUI) }
    }
}
