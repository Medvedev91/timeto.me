package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.*
import timeto.shared.db.EventModel
import timeto.shared.db.RepeatingModel
import timeto.shared.db.TaskFolderModel
import timeto.shared.db.TaskModel
import timeto.shared.ui.sortedByFolder

/**
 * todo Live update? tasksUI + curTimeString
 */
class TmrwPeekVM : __VM<TmrwPeekVM.State>() {

    class TaskUI(task: TaskModel) : timeto.shared.ui.TaskUI(task)

    data class State(
        val tasksUI: List<TaskUI>,
        val curTimeString: String,
    )

    override val state = MutableStateFlow(
        State(
            tasksUI = listOf(),
            curTimeString = " "
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        scope.launchEx {

            val rawTasks = mutableListOf<TaskModel>()
            val unixTmrwDS = UnixTime(utcOffset = localUtcOffsetWithDayStart).inDays(1)
            val tmrwDSDay = unixTmrwDS.localDay
            var lastFakeTaskId = unixTmrwDS.localDayStartTime()

            // Tasks
            TaskModel.getAsc()
                .filter { it.isTmrw }
                .let { rawTasks.addAll(it) }

            // Repeatings
            RepeatingModel.getAsc()
                .filter { it.getNextDay() == tmrwDSDay }
                .forEach { repeating ->
                    rawTasks.add(
                        TaskModel(
                            id = ++lastFakeTaskId,
                            text = repeating.prepTextForTask(tmrwDSDay),
                            folder_id = TaskFolderModel.ID_TODAY,
                        )
                    )
                }

            // Events
            EventModel.getAscByTime()
                .filter { it.getLocalTime().localDay == tmrwDSDay }
                .forEach { event ->
                    rawTasks.add(
                        TaskModel(
                            id = ++lastFakeTaskId,
                            text = event.prepTextForTask(),
                            folder_id = TaskFolderModel.ID_TODAY,
                        )
                    )
                }

            val resTasks = rawTasks
                .map { TaskUI(it) }
                .sortedByFolder(DI.getTodayFolder())
            state.update {
                it.copy(
                    tasksUI = resTasks,
                    curTimeString = getCurTimeString(unixTmrwDS)
                )
            }
        }
    }
}

private fun getCurTimeString(
    unixTime: UnixTime
): String = unixTime.getStringByComponents(
    listOf(
        UnixTime.StringComponent.dayOfMonth,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.month,
        UnixTime.StringComponent.comma,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.dayOfWeek,
    )
)
