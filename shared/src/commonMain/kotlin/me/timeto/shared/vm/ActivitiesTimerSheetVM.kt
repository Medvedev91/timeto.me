package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.DI
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.onEachExIn
import me.timeto.shared.textFeatures
import me.timeto.shared.db.ActivityModel__Data.TimerHints.TimerHintUI

class ActivitiesTimerSheetVM(
    private val timerContext: ActivityTimerSheetVM.TimerContext?,
) : __VM<ActivitiesTimerSheetVM.State>() {

    companion object {

        fun prepHistorySecondsMap(
            taskText: String,
        ): Map<Int, List<Int>> /* activity id => seconds list */ {
            return DI.hotIntervalsDesc
                .filter { taskText.lowercase() == it.note?.lowercase() }
                .groupBy { it.activity_id }
                .map {
                    it.key to it.value.map { it.timer }.distinct()
                }
                .toMap()
        }

        private fun prepActivitiesUI(
            timerContext: ActivityTimerSheetVM.TimerContext?,
            sortedActivities: List<ActivityModel>,
        ): List<ActivityUI> {

            return sortedActivities.map { activity ->

                val timerHints = activity.getData().timer_hints.getTimerHintsUI(
                    historyLimit = 3,
                    customLimit = 6,
                    onSelect = { hintUI ->
                        when (timerContext) {
                            is ActivityTimerSheetVM.TimerContext.Task ->
                                timerContext.task.startInterval(hintUI.seconds, activity)
                            null -> activity.startInterval(hintUI.seconds)
                        }
                    }
                )

                ActivityUI(
                    activity = activity,
                    timerHints = timerHints
                )
            }
        }
    }

    class ActivityUI(
        val activity: ActivityModel,
        val timerHints: List<TimerHintUI>,
    ) {
        val listText = activity.name.textFeatures().textUi()
        val isActive = DI.lastInterval.activity_id == activity.id
    }

    data class State(
        val allActivities: List<ActivityUI>,
    )

    override val state = MutableStateFlow(
        State(
            allActivities = prepActivitiesUI(timerContext, DI.activitiesSorted),
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        ActivityModel.getAscSortedFlow().onEachExIn(scope) { activities ->
            state.update {
                it.copy(allActivities = prepActivitiesUI(timerContext, activities))
            }
        }
    }
}
