package me.timeto.shared.vm

import kotlinx.coroutines.flow.*

class ActivityPomodoroSheetVm(
    selectedTime: Int,
) : __VM<ActivityPomodoroSheetVm.State>() {

    data class State(
        val selectedTime: Int,
    ) {

        val headerTitle = "Pomodoro"
        val doneTitle = "Done"

        val listItemsUi: List<ListItemUi> = pomodoroTimes.map { time ->
            val timeStr: String = when {
                time == 0 -> "None"
                time < 3_600 -> "${time / 60} min"
                time == 3600 -> "1 hour"
                else -> throw Exception("Invalid time")
            }
            ListItemUi(
                time = time,
                text = timeStr,
                isSelected = selectedTime == time,
            )
        }

        fun prepSelectedTime(): Int = listItemsUi.firstOrNull { it.isSelected }?.time ?: 0
    }

    override val state = MutableStateFlow(
        State(
            selectedTime = selectedTime,
        )
    )

    fun setTime(time: Int) {
        state.update { it.copy(selectedTime = time) }
    }

    ///

    data class ListItemUi(
        val time: Int,
        val text: String,
        val isSelected: Boolean,
    )
}

private val pomodoroTimes = listOf(0, 5, 10, 15, 30, 60).map { it * 60 }
