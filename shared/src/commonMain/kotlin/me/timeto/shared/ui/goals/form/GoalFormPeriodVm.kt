package me.timeto.shared.ui.goals.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.UnixTime
import me.timeto.shared.db.GoalDb
import me.timeto.shared.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.vm.__Vm

class GoalFormPeriodVm(
    initGoalDbPeriod: GoalDb.Period?,
) : __Vm<GoalFormPeriodVm.State>() {

    data class State(
        val selectedDaysOfWeek: Set<Int>,
    ) {

        val title = "Period"
        val doneText = "Done"

        fun buildPeriodOrNull(
            dialogsManager: DialogsManager,
        ): GoalDb.Period? = try {
            GoalDb.Period.DaysOfWeek.buildWithValidation(selectedDaysOfWeek)
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
            null
        }
    }

    override val state = MutableStateFlow(
        State(
            selectedDaysOfWeek = when (initGoalDbPeriod) {
                is GoalDb.Period.DaysOfWeek -> initGoalDbPeriod.days.toSet()
                else -> (0..6).toSet()
            },
        )
    )

    ///

    val daysOfWeek: List<DayOfWeek> = UnixTime.dayOfWeekNames
        .mapIndexed { dayIdx, dayTitle ->
            DayOfWeek(
                id = dayIdx,
                title = dayTitle,
            )
        }

    fun toggleDayOfWeek(dayOfWeek: DayOfWeek) {
        state.update { state ->
            val newDayOfWeek: MutableSet<Int> =
                state.selectedDaysOfWeek.toMutableSet()
            if (newDayOfWeek.contains(dayOfWeek.id))
                newDayOfWeek.remove(dayOfWeek.id)
            else
                newDayOfWeek.add(dayOfWeek.id)
            state.copy(selectedDaysOfWeek = newDayOfWeek)
        }
    }

    ///

    data class DayOfWeek(
        val id: Int,
        val title: String,
    )
}
