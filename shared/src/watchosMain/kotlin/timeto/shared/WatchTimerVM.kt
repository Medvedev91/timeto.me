package timeto.shared

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timeto.shared.db.IntervalModel
import timeto.shared.vm.__VM
import timeto.shared.vm.ui.TimerDataUI

class WatchTimerVM : __VM<WatchTimerVM.State>() {

    /**
     * todo
     * Don't use "data" class because the class has
     * calculated fields that don't trigger UI updates.
     */
    class State(
        val isCountDown: Boolean,
        val lastInterval: IntervalModel,
    ) {
        private val timerData = TimerDataUI(lastInterval, ColorNative.text)
        val timeNote = if (isCountDown) timerData.title else timerData.timePassedNote
        val color = if (isCountDown) timerData.color else ColorNative.purple
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
