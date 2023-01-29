package timeto.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timeto.shared.db.TaskModel
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
        val folders: List<FolderUI>
    )

    override val state = MutableStateFlow(
        State(
            folders = listOf()
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        TaskModel.getAscFlow().onEachExIn(scope) { tasks ->
            upFolders(tasks)
        }
    }

    private fun upFolders(allTasks: List<TaskModel>) {
        val folders = listOf(
            FolderUI(title = "Today", tasks = allTasks.filter { it.isToday }.map { TaskUI(it) }),
            FolderUI(title = "Week", tasks = allTasks.filter { it.isWeek }.map { TaskUI(it) }),
            FolderUI(title = "Inbox", tasks = allTasks.filter { it.isInbox }.map { TaskUI(it) }),
        )
        state.update { it.copy(folders = folders) }
    }
}
