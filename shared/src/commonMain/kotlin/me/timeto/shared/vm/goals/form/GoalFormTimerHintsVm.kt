package me.timeto.shared.vm.goals.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.vm.Vm

class GoalFormTimerHintsVm(
    initTimerHints: List<Int>,
) : Vm<GoalFormTimerHintsVm.State>() {

    data class State(
        val timerHintsUi: List<TimerHintUi>,
    )

    override val state = MutableStateFlow(
        State(
            timerHintsUi = TimerHintUi.buildList(initTimerHints),
        )
    )

    fun add(seconds: Int) {
        state.update { state ->
            val timerHints: List<Int> =
                state.timerHintsUi.map { it.seconds } + seconds
            state.copy(timerHintsUi = TimerHintUi.buildList(timerHints))
        }
    }

    fun delete(seconds: Int) {
        state.update { state ->
            val timerHints: List<Int> =
                state.timerHintsUi.map { it.seconds }.filter { it != seconds }
            state.copy(timerHintsUi = TimerHintUi.buildList(timerHints))
        }
    }

    fun getTimerHints(): List<Int> =
        state.value.timerHintsUi.map { it.seconds }

    ///

    data class TimerHintUi(
        val seconds: Int,
    ) {

        val text: String =
            seconds.toTimerHintNote(isShort = false)

        companion object {

            fun buildList(timerHints: List<Int>): List<TimerHintUi> =
                timerHints.toSet().sorted().map { TimerHintUi(it) }
        }
    }
}
