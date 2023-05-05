package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.DI
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.textFeatures
import me.timeto.shared.ui.TimerHintUI

class ActivitiesTimerSheetVM(
    timerContext: ActivityTimerSheetVM.TimerContext?,
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
            when (timerContext) {
                is ActivityTimerSheetVM.TimerContext.Task ->
                    prepHistorySecondsMap(timerContext.task.text)
                null -> mapOf()
            }

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
                            when (timerContext) {
                                is ActivityTimerSheetVM.TimerContext.Task ->
                                    timerContext.task.startInterval(seconds, activity)
                                null -> activity.startInterval(seconds)
                            }
                        }
                    )
                },
            )
        )
    }
}
