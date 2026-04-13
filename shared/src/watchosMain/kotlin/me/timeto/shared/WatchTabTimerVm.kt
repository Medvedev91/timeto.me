package me.timeto.shared

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.vm.Vm

class WatchTabTimerVm : Vm<WatchTabTimerVm.State>() {

    class ActivityUi(
        val activityDb: ActivityDb,
    ) {

        val text: String =
            activityDb.name.textFeatures().textUi()

        val timerHintsUi: List<TimerHintUi> = listOf(5 * 60, 15 * 60, 45 * 60).map { timer ->
            TimerHintUi(
                timer = timer,
                onStart = {
                    WatchToIosSync.startIntervalWithLocal(
                        activityDb = activityDb,
                        timer = timer,
                    )
                },
            )
        }

        fun startDefaultTimer() {
            WatchToIosSync.startIntervalWithLocal(
                activityDb = activityDb,
                timer = null,
            )
        }
    }

    data class State(
        val activitiesDb: List<ActivityDb>,
        val lastInterval: IntervalDb,
        val isPurple: Boolean,
    ) {
        val activitiesUi = activitiesDb.toUiList(lastInterval)
    }

    override val state = MutableStateFlow(
        State(
            activitiesDb = Cache.activitiesDb,
            lastInterval = Cache.lastIntervalDb,
            isPurple = false,
        )
    )

    init {
        val scope = scopeVm()
        ActivityDb.selectAllFlow().onEachExIn(scope) { activitiesDb ->
            state.update { it.copy(activitiesDb = activitiesDb) }
        }
        IntervalDb.selectLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) { interval ->
                state.update { it.copy(lastInterval = interval, isPurple = false) }
            }
    }

    ///

    class TimerHintUi(
        val timer: Int,
        val onStart: suspend () -> Unit,
    ) {

        val text: String =
            timer.toTimerHintNote(isShort = true)

        fun startInterval() {
            launchExIo {
                onStart()
            }
        }
    }
}

private fun List<ActivityDb>.toUiList(
    lastInterval: IntervalDb,
): List<WatchTabTimerVm.ActivityUi> {
    // On top the active activity :)
    val sorted = this.sortedByDescending { it.id == lastInterval.activityId }
    return sorted.mapIndexed { idx, activity ->
        WatchTabTimerVm.ActivityUi(
            activityDb = activity,
        )
    }
}
