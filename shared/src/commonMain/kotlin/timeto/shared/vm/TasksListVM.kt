package timeto.shared.vm

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timeto.shared.*
import timeto.shared.db.TaskFolderModel
import timeto.shared.db.TaskModel

class TasksListVM(
    val folder: TaskFolderModel,
) : __VM<TasksListVM.State>() {

    class TaskUI(
        val task: TaskModel,
    ) {

        val textFeatures = TextFeatures.parse(task.text)
        val listText = textFeatures.textUI()

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
        .map { TaskUI(it) }
        .let { tasksUI ->
            if (folder.id == TaskFolderModel.ID_TODAY)
                tasksUI.sortedForToday()
            else
                tasksUI.sortedByDescending { it.task.id }
        }
}

private fun List<TasksListVM.TaskUI>.sortedForToday(): List<TasksListVM.TaskUI> = this
    // Map<Int /* day */, List<TasksListVM.TaskUI>>
    .groupBy { taskUI ->
        taskUI.textFeatures.fromRepeating?.day
            ?: taskUI.textFeatures.timeUI?.unixTime?.localDay
            ?: taskUI.task.unixTime().localDay
    }
    .toList()
    .sortedByDescending { it.first }
    // List<List<TasksListVM.TaskUI>>
    .map { it.second.sortedInsideDay() }
    .flatten()

private fun List<TasksListVM.TaskUI>.sortedInsideDay(): List<TasksListVM.TaskUI> {
    val (tasksWithDaytime, tasksNoDaytime) = this.partition { it.textFeatures.timeUI != null }
    val minTaskIdWithDaytime = tasksWithDaytime.minOfOrNull { it.task.id } ?: Int.MIN_VALUE
    val (tasksBeforeDaytime, tasksAfterDaytime) = tasksNoDaytime.partition { it.task.id < minTaskIdWithDaytime }

    val resList = mutableListOf<TasksListVM.TaskUI>()
    tasksAfterDaytime.forEach { resList.add(it) }
    tasksWithDaytime
        .sortedBy { it.textFeatures.timeUI!!.unixTime.time }
        .forEach { resList.add(it) }
    tasksBeforeDaytime.forEach { resList.add(it) }

    return resList
}
