package timeto.shared.vm

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timeto.shared.*
import timeto.shared.db.TaskFolderModel
import timeto.shared.db.TaskModel
import timeto.shared.vm.ui.DaytimeUI

class TasksListVM(
    val folder: TaskFolderModel,
) : __VM<TasksListVM.State>() {

    class TaskUI(
        val task: TaskModel,
    ) {
        val daytimeUI: DaytimeUI?
        val listText: String
        val triggers: List<Trigger>

        init {
            val textFeatures = TextFeatures.parse(task.text)
            listText = textFeatures.textUI()
            triggers = textFeatures.triggers
            daytimeUI = textFeatures.toDaytimeUIOrNull(withTimeLeft = true)
        }

        fun start(
            onStarted: () -> Unit,
            needSheet: () -> Unit, // todo data for sheet
        ) {
            val autostartData = taskAutostartData(task) ?: return needSheet()
            launchExDefault {
                task.startInterval(
                    deadline = autostartData.second,
                    activity = autostartData.first,
                )
                onStarted()
            }
        }

        fun upFolder(newFolder: TaskFolderModel) {
            launchExDefault {
                task.upFolder(newFolder)
            }
        }

        // - By manual removing;
        // - After adding to calendar;
        // - By starting from activity sheet;
        fun delete() {
            launchExDefault {
                task.delete()
            }
        }
    }

    data class State(
        val tasksUI: List<TaskUI>,
    )

    override val state = MutableStateFlow(
        State(
            tasksUI = DI.tasks.toUiList()
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        TaskModel.getAscFlow()
            .onEachExIn(scope) { list ->
                state.update { it.copy(tasksUI = list.toUiList()) }
            }
        // To update daytime badges
        scope.launch {
            while (true) {
                delayToNextMinute()
                state.update { it.copy(tasksUI = TaskModel.getAsc().toUiList()) }
            }
        }
    }

    private fun List<TaskModel>.toUiList() = this
        .filter { it.folder_id == folder.id }
        .sortedWith(compareByDescending { it.id })
        .map { TaskUI(it) }
}
