package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.UnixTime
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.toTimerHintNote

class GoalPickerSheetVM : __VM<GoalPickerSheetVM.State>() {

    data class State(
        val seconds: Int,
        val weekDays: List<Int>,
    ) {
        val headerTitle = "New Goal"
        val doneTitle = "Add"

        val timeTitle = "Time"
        val timeNote = seconds.toTimerHintNote(isShort = false)

        val timerPickerSheetTitle = "Time"

        val weekDaysUI: List<WeekDayUI> = UnixTime.dayOfWeekNames1.mapIndexed { idx, title ->
            WeekDayUI(idx = idx, title = title, isSelected = idx in weekDays)
        }

        val goal = ActivityModel.Goal(
            seconds = seconds,
            period = ActivityModel.Goal.Period.DaysOfWeek(
                weekDays = weekDaysUI.filter { it.isSelected }.map { it.idx },
            )
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

    ///

    data class WeekDayUI(
        val idx: Int,
        val title: String,
        val isSelected: Boolean,
    )
}
