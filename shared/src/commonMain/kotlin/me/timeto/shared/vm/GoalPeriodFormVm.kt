package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.GoalDb
import me.timeto.shared.showUiAlert

class GoalPeriodFormVm(
    initPeriod: GoalDb.Period?,
) : __Vm<GoalPeriodFormVm.State>() {

    data class State(
        val period: GoalDb.Period?,
        val selectedType: GoalDb.Period.Type,
        val daysOfWeek: List<Int>,
    ) {

        val headerTitle = "Period"
        val headerDoneText = "Done"

        val daysOfWeekTitle = "Days of Week"
        val isDaysOfWeekSelected: Boolean = selectedType == GoalDb.Period.Type.daysOfWeek
    }

    override val state = MutableStateFlow(
        State(
            period = initPeriod,
            selectedType = initPeriod?.type ?: GoalDb.Period.Type.daysOfWeek,
            daysOfWeek = if (initPeriod is GoalDb.Period.DaysOfWeek)
                initPeriod.days else listOf(0, 1, 2, 3, 4),
        )
    )

    fun setDaysOfWeek(daysOfWeek: List<Int>) {
        state.update { it.copy(daysOfWeek = daysOfWeek) }
    }

    fun buildPeriod(
        onSuccess: (GoalDb.Period) -> Unit,
    ) {
        val period: GoalDb.Period? = state.value.period
        if (period == null) {
            showUiAlert("Period not selected")
            return
        }
        onSuccess(period)
    }
}
