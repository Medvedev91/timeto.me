package me.timeto.shared

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.timeto.shared.db.IntervalModel
import me.timeto.shared.vm.__VM
import me.timeto.shared.vm.ui.TimerDataUI

class WatchTimerVM : __VM<WatchTimerVM.State>() {

    /**
     * todo refactor
     * Don't use "data" class because the class has
     * calculated fields that don't trigger UI updates.
     */
    class State(
        val isCountDown: Boolean,
        val lastInterval: IntervalModel,
    ) {
        val timerData = TimerDataUI(lastInterval, isCountDown, ColorNative.text)
    }

    override val state = MutableStateFlow(
        State(
            isCountDown = true,
            lastInterval = DI.lastInterval
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        IntervalModel.getLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) { newInterval ->
                state.update {
                    State(
                        isCountDown = true,
                        lastInterval = newInterval,
                    )
                }
            }
        scope.launch {
            while (true) {
                delay(1_000L)
                state.update {
                    State(
                        isCountDown = it.isCountDown,
                        lastInterval = it.lastInterval,
                    )
                }
            }
        }
    }

    fun toggleIsCountDown() {
        state.update {
            State(
                isCountDown = !it.isCountDown,
                lastInterval = it.lastInterval,
            )
        }
    }
}
