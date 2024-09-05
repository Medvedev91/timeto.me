package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.UIException
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.showUiAlert
import me.timeto.shared.toTimerHintNote

class GoalPickerSheetVm : __Vm<GoalPickerSheetVm.State>() {

    data class State(
        val seconds: Int,
        val weekDays: List<Int>,
    ) {
        val headerTitle = "New Goal"
        val doneTitle = "Add"

        val durationTitle = "Duration"
        val durationNote = seconds.toTimerHintNote(isShort = false)

        val timerPickerSheetTitle = "Duration"

        fun buildGoal(
            onBuild: (goal: ActivityDb.Goal) -> Unit
        ) {
            try {
                val goal = ActivityDb.Goal(
                    seconds = seconds,
                    period = ActivityDb.Goal.Period.DaysOfWeek(weekDays = weekDays),
                )
                goal.period.assertValidation()
                onBuild(goal)
            } catch (e: UIException) {
                showUiAlert(e.uiMessage)
            }
        }
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
