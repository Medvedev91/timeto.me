package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.toTimerHintNote

class GoalPickerSheetVM : __VM<GoalPickerSheetVM.State>() {

    data class State(
        val seconds: Int,
        val weekDays: List<Int>,
    ) {
        val headerTitle = "New Goal"
        val doneTitle = "Add"

        val durationTitle = "Duration"
        val durationNote = seconds.toTimerHintNote(isShort = false)

        val timerPickerSheetTitle = "Duration"

        val goal = ActivityModel.Goal(
            seconds = seconds,
            period = ActivityModel.Goal.Period.DaysOfWeek(weekDays = weekDays),
        )
    }

    override val state = MutableStateFlow(
        State(
            seconds = 3_600,
            weekDays = (0..6).toList(),
        )
    )

    fun upWeekDays(newWeekDays: List<Int>) {
        state.update { it.copy(weekDays = newWeekDays) }
    }

    fun upTime(seconds: Int) {
        state.update { it.copy(seconds = seconds) }
    }
}
