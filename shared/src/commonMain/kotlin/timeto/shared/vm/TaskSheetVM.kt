package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.DI
import timeto.shared.db.ActivityModel
import timeto.shared.db.TaskModel
import timeto.shared.vm.ui.TimerHintUI

class TaskSheetVM(
    val task: TaskModel,
) : __VM<TaskSheetVM.State>() {

    companion object {

        fun prepHistorySecondsMap(
            taskText: String,
        ): Map<Int, List<Int>> /* activity id => seconds list */ {
            return DI.hotIntervalsDesc
                .filter { taskText.lowercase() == it.note?.lowercase() }
                .groupBy { it.activity_id }
                .map {
                    it.key to it.value.map { it.deadline }.distinct()
                }
                .toMap()
        }
    }

    inner class ActivityUI(
        val activity: ActivityModel,
        val historySeconds: List<Int>,
    ) {
        val timerHints = TimerHintUI.buildList(
            activity,
            isShort = true,
            historyLimit = 3,
            customLimit = 6,
            primaryHints = historySeconds,
        ) { seconds ->
            task.startInterval(seconds, activity)
        }
    }

    data class State(
        val allActivities: List<ActivityUI>,
    )

    override val state: MutableStateFlow<State>

    init {
        val historySecondsMap = prepHistorySecondsMap(task.text)
        state = MutableStateFlow(
            State(
                allActivities = DI.activitiesSorted.map { activity ->
                    ActivityUI(
                        activity = activity,
                        historySeconds = historySecondsMap[activity.id] ?: listOf(),
                    )
                },
            )
        )
    }
}
