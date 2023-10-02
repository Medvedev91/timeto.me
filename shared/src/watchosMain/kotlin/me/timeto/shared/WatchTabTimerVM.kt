package me.timeto.shared

import kotlinx.coroutines.flow.*
import me.timeto.shared.data.TimerTabActivityData
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.db.IntervalModel
import me.timeto.shared.vm.__VM

class WatchTabTimerVM : __VM<WatchTabTimerVM.State>() {

    class ActivityUI(
        val activity: ActivityModel,
        val lastInterval: IntervalModel,
        val isPurple: Boolean,
    ) {

        val data = TimerTabActivityData(activity, lastInterval, isPurple)

        val timerHints = activity.data.timer_hints.getTimerHintsUI(
            historyLimit = 4,
            customLimit = 4,
        ) { hintUI ->
            WatchToIosSync.startIntervalWithLocal(
                activity = activity,
                timer = hintUI.seconds,
            )
        }
    }

    data class State(
        val activities: List<ActivityModel>,
        val lastInterval: IntervalModel,
        val isPurple: Boolean,
    ) {
        val activitiesUI = activities.toUiList(lastInterval, isPurple)
    }

    override val state = MutableStateFlow(
        State(
            activities = DI.activitiesSorted,
            lastInterval = DI.lastInterval,
            isPurple = false,
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        ActivityModel.getAscSortedFlow()
            .onEachExIn(scope) { activities ->
                state.update { it.copy(activities = activities) }
            }
        IntervalModel.getLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) { interval ->
                state.update { it.copy(lastInterval = interval, isPurple = false) }
            }
    }
}

private fun List<ActivityModel>.toUiList(
    lastInterval: IntervalModel,
    isPurple: Boolean,
): List<WatchTabTimerVM.ActivityUI> {
    // On top the active activity :)
    val sorted = this.sortedByDescending { it.id == lastInterval.activity_id }
    return sorted.mapIndexed { idx, activity ->
        WatchTabTimerVM.ActivityUI(
            activity = activity,
            lastInterval = lastInterval,
            isPurple = isPurple,
        )
    }
}
