package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.toHms

class ActivityPomodoroSheetVm(
    selectedTimer: Int,
) : __VM<ActivityPomodoroSheetVm.State>() {

    companion object {

        fun prepPomodoroTimeString(timer: Int): String = when {
            timer < 0 -> throw Exception("prepPomodoroTimeString(0)")
            timer == 0 -> "None"
            timer < 3_600 -> "${timer / 60} min"
            else -> {
                val (h, m, _) = timer.toHms()
                if (m == 0)
                    "$h ${if (h == 1) "hour" else "hours"}"
                else
                    "${h}h ${m}m"
            }
        }
    }

    data class State(
        val selectedTimer: Int,
    ) {

        val headerTitle = "Pomodoro"
        val doneTitle = "Done"

        val listItemsUi: List<ListItemUi> = pomodoroTimers.map { timer ->
            ListItemUi(
                time = timer,
                text = prepPomodoroTimeString(timer),
                isSelected = selectedTimer == timer,
            )
        }

        fun prepSelectedTime(): Int = listItemsUi.firstOrNull { it.isSelected }?.time ?: 0
    }

    override val state = MutableStateFlow(
        State(
            selectedTimer = selectedTimer,
        )
    )

    fun setTimer(time: Int) {
        state.update { it.copy(selectedTimer = time) }
    }

    ///

    data class ListItemUi(
        val time: Int,
        val text: String,
        val isSelected: Boolean,
    )
}

private val pomodoroTimers = listOf(0, 5, 10, 15, 30, 60).map { it * 60 }
