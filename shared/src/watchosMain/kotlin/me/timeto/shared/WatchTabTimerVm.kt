package me.timeto.shared

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.vm.Vm

class WatchTabTimerVm : Vm<WatchTabTimerVm.State>() {

    class ActivityUI(
        val goalDb: Goal2Db,
    ) {

        val text: String =
            goalDb.name.textFeatures().textUi()

        val timerHintsUi: List<TimerHintUi> = listOf(5 * 60, 15 * 60, 45 * 60).map { timer ->
            TimerHintUi(
                timer = timer,
                onStart = {
                    WatchToIosSync.startIntervalWithLocal(
                        goalDb = goalDb,
                        timer = timer,
                    )
                },
            )
        }

        fun startDefaultTimer() {
            WatchToIosSync.startIntervalWithLocal(
                goalDb = goalDb,
                timer = null,
            )
        }
    }

    data class State(
        val activities: List<Goal2Db>,
        val lastInterval: IntervalDb,
        val isPurple: Boolean,
    ) {
        val activitiesUI = activities.toUiList(lastInterval)
    }

    override val state = MutableStateFlow(
        State(
            activities = Cache.goals2Db,
            lastInterval = Cache.lastIntervalDb,
            isPurple = false,
        )
    )

    init {
        val scope = scopeVm()
        Goal2Db.selectAllFlow()
            .onEachExIn(scope) { activities ->
                state.update { it.copy(activities = activities) }
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

private fun List<Goal2Db>.toUiList(
    lastInterval: IntervalDb,
): List<WatchTabTimerVm.ActivityUI> {
    // On top the active activity :)
    val sorted = this.sortedByDescending { it.id == lastInterval.goal_id }
    return sorted.mapIndexed { idx, activity ->
        WatchTabTimerVm.ActivityUI(
            goalDb = activity,
        )
    }
}
