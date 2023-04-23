package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.DI
import timeto.shared.db.ActivityModel
import timeto.shared.db.TaskModel
import timeto.shared.textFeatures
import timeto.shared.ui.TimerHintUI

class ActivitiesTimerSheetVM(
    val task: TaskModel,
) : __VM<ActivitiesTimerSheetVM.State>() {

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

    class ActivityUI(
        val activity: ActivityModel,
        val timerHints: List<TimerHintUI>,
    ) {
        val listText = activity.name.textFeatures().textUi()
    }

    data class State(
        val allActivities: List<ActivityUI>,
    )

    override val state: MutableStateFlow<State>

    init {
        val primarySecondsMap: Map<Int, List<Int>> /* `activity id` -> `seconds list` */ =
            if (task != null) prepHistorySecondsMap(task.text) else mapOf()

        state = MutableStateFlow(
            State(
                allActivities = DI.activitiesSorted.map { activity ->
                    val primarySeconds = primarySecondsMap[activity.id] ?: listOf()
                    ActivityUI(
                        activity = activity,
                        timerHints = TimerHintUI.buildList(
                            activity,
                            isShort = true,
                            historyLimit = 3,
                            customLimit = 6,
                            primaryHints = primarySeconds,
                        ) { seconds ->
                            task.startInterval(seconds, activity)
                        }
                    )
                },
            )
        )
    }
}
