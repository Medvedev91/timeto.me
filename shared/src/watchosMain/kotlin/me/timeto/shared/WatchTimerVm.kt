package me.timeto.shared

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.ui.__Vm

class WatchTimerVm : __Vm<WatchTimerVm.State>() {

    data class State(
        val isPurple: Boolean,
        val lastInterval: IntervalDb,
        val idToUpdate: Int = 0,
    ) {
        // todo
        val timerData = TimerStateUi(lastInterval, listOf(), isPurple)
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
                delay(1_000L)
                state.update { it.copy(idToUpdate = it.idToUpdate + 1) }
            }
        }
    }

    fun togglePomodoro() {
        WatchToIosSync.togglePomodoro()
    }
}
