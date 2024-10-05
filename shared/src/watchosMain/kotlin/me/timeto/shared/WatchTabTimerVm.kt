package me.timeto.shared

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.vm.__Vm

class WatchTabTimerVm : __Vm<WatchTabTimerVm.State>() {

    class ActivityUI(
        val activity: ActivityDb,
    ) {

        val text: String = activity.name.textFeatures().textUi()

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
        val activities: List<ActivityDb>,
        val lastInterval: IntervalDb,
        val isPurple: Boolean,
    ) {
        val activitiesUI = activities.toUiList(lastInterval)
    }

    override val state = MutableStateFlow(
        State(
            activities = Cache.activitiesDbSorted,
            lastInterval = Cache.lastInterval,
            isPurple = false,
        )
    )

    override fun onAppear() {
        val scope = scopeVm()
        ActivityDb.getAscSortedFlow()
            .onEachExIn(scope) { activities ->
                state.update { it.copy(activities = activities) }
            }
        IntervalDb.getLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) { interval ->
                state.update { it.copy(lastInterval = interval, isPurple = false) }
            }
    }
}

private fun List<ActivityDb>.toUiList(
    lastInterval: IntervalDb,
): List<WatchTabTimerVm.ActivityUI> {
    // On top the active activity :)
    val sorted = this.sortedByDescending { it.id == lastInterval.activity_id }
    return sorted.mapIndexed { idx, activity ->
        WatchTabTimerVm.ActivityUI(
            activity = activity,
        )
    }
}
