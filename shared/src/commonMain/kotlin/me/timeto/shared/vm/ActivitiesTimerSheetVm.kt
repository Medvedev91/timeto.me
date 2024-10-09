package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.Cache
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.onEachExIn
import me.timeto.shared.textFeatures
import me.timeto.shared.db.ActivityDb__Data.TimerHints.TimerHintUI

class ActivitiesTimerSheetVm(
    private val timerContext: ActivityTimerSheetVm.TimerContext?,
) : __Vm<ActivitiesTimerSheetVm.State>() {

    companion object {

        private fun prepActivitiesUI(
            timerContext: ActivityTimerSheetVm.TimerContext?,
            sortedActivities: List<ActivityDb>,
        ): List<ActivityUI> = sortedActivities.map { activity ->

            val timerHints = activity.data.timer_hints.getTimerHintsUI(
                historyLimit = 3,
                customLimit = 6,
                onSelect = { hintUI ->
                    ActivityTimerSheetVm.startIntervalByContext(timerContext, activity, hintUI.seconds)
                }
            )

            ActivityUI(
                activity = activity,
                timerHints = timerHints
            )
        }
    }

    class ActivityUI(
        val activity: ActivityDb,
        val timerHints: List<TimerHintUI>,
    ) {
        val listText = activity.name.textFeatures().textUi()
        val isActive = Cache.lastInterval.activity_id == activity.id
    }

    data class State(
        val allActivities: List<ActivityUI>,
    )

    override val state = MutableStateFlow(
        State(
            allActivities = prepActivitiesUI(timerContext, Cache.activitiesDbSorted),
        )
    )

    override fun onAppear() {
        val scope = scopeVm()
        ActivityDb.selectAllSortedFlow().onEachExIn(scope) { activities ->
            state.update {
                it.copy(allActivities = prepActivitiesUI(timerContext, activities))
            }
        }
    }
}
