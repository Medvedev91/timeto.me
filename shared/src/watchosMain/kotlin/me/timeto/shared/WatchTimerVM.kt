package me.timeto.shared

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.vm.__VM
import me.timeto.shared.vm.ui.TimerDataUI

class WatchTimerVM : __VM<WatchTimerVM.State>() {

    data class State(
        val isPurple: Boolean,
        val lastInterval: IntervalDb,
        val idToUpdate: Int = 0,
    ) {
        // todo
        val timerData = TimerDataUI(lastInterval, listOf(), isPurple)
    }

    override val state = MutableStateFlow(
        State(
            isPurple = false,
            lastInterval = DI.lastInterval
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        IntervalDb.getLastOneOrNullFlow()
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

    fun toggleIsPurple() {
        state.update { it.copy(isPurple = !it.isPurple) }
    }
}
