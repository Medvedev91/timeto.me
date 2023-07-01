package me.timeto.shared

import kotlinx.coroutines.flow.*
import me.timeto.shared.data.TimerTabActivityData
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.db.IntervalModel
import me.timeto.shared.vm.__VM
import me.timeto.shared.ui.TimerHintUI

class WatchTabTimerVM : __VM<WatchTabTimerVM.State>() {

    class ActivityUI(
        val activity: ActivityModel,
        val lastInterval: IntervalModel,
    ) {

        val data = TimerTabActivityData(activity, lastInterval)

        val timerHints = TimerHintUI.buildList(
            activity,
            isShort = true,
            historyLimit = 4,
            customLimit = 4,
        ) { seconds ->
            WatchToIosSync.startIntervalWithLocal(
                activity = activity,
                deadline = seconds,
            )
        }
    }

    data class State(
        val lastInterval: IntervalModel,
        val activitiesUI: List<ActivityUI>,
    )

    override val state = MutableStateFlow(
        prepState(DI.lastInterval, DI.activitiesSorted)
    )

    override fun onAppear() {
        val scope = scopeVM()
        IntervalModel.getLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) { lastInterval ->
                state.update {
                    prepState(lastInterval, ActivityModel.getAscSorted())
                }
            }
        // In case if hints would change
        ActivityModel.getAscSortedFlow()
            .onEachExIn(scope) { activities ->
                state.update {
                    prepState(IntervalModel.getLastOneOrNull()!!, activities)
                }
            }
    }
}

private fun prepState(
    lastInterval: IntervalModel,
    activities: List<ActivityModel>,
) = WatchTabTimerVM.State(
    lastInterval = lastInterval,
    activitiesUI = activities.toUiList(lastInterval)
)

private fun List<ActivityModel>.toUiList(
    lastInterval: IntervalModel
): List<WatchTabTimerVM.ActivityUI> {
    // On top the active activity :)
    val sorted = this.sortedByDescending { it.id == lastInterval.activity_id }
    return sorted.mapIndexed { idx, activity ->
        WatchTabTimerVM.ActivityUI(
            activity = activity,
            lastInterval = lastInterval,
        )
    }
}
