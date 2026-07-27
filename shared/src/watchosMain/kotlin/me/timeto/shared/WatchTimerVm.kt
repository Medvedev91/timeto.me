package me.timeto.shared

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.vm.Vm
import kotlin.time.Duration.Companion.milliseconds

class WatchTimerVm : Vm<WatchTimerVm.State>() {

    data class State(
        val isPurple: Boolean,
        val lastInterval: IntervalDb,
        val idToUpdate: Int = 0,
    ) {
        // todo
        val timerData = TimerStateUi(
            intervalUi = IntervalUi(
                intervalDb = lastInterval,
                activityDb = lastInterval.selectActivityDbCached(),
            ),
            todayTasksDb = listOf(),
            isPurple = isPurple,
        )
    }

    override val state = MutableStateFlow(
        State(
            isPurple = false,
            lastInterval = Cache.lastIntervalDb
        )
    )

    init {
        val scope = scopeVm()
        IntervalDb.selectLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) { newInterval ->
                state.update { it.copy(isPurple = false, lastInterval = newInterval) }
            }
        scope.launch {
            while (true) {
                delay(1_000.milliseconds)
                state.update { it.copy(idToUpdate = it.idToUpdate + 1) }
            }
        }
    }

    fun togglePomodoro() {
        WatchToIosSync.togglePomodoro()
    }
}
