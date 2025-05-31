package me.timeto.shared.vm.activities.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.vm.__Vm

class ActivityFormTimerHintsVm(
    initTimerHints: Set<Int>,
) : __Vm<ActivityFormTimerHintsVm.State>() {

    data class State(
        val timerHints: Set<Int>,
    ) {
        val timerHintsUi: List<TimerHintUi> =
            timerHints.sorted().map { TimerHintUi(seconds = it) }
    }

    override val state = MutableStateFlow(
        State(
            timerHints = initTimerHints,
        )
    )

    ///

    fun add(seconds: Int) {
        state.update {
            it.copy(timerHints = it.timerHints + seconds)
        }
    }

    fun delete(seconds: Int) {
        state.update {
            it.copy(timerHints = it.timerHints - seconds)
        }
    }

    ///

    data class TimerHintUi(
        val seconds: Int,
    ) {
        val text: String =
            seconds.toTimerHintNote(isShort = false)
    }
}
