package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.GoalDb
import me.timeto.shared.showUiAlert

class GoalPeriodFormVm(
    initPeriod: GoalDb.Period?,
) : __Vm<GoalPeriodFormVm.State>() {

    data class State(
        val period: GoalDb.Period?,
    ) {

        val headerTitle = "Period"
        val headerDoneText = "Done"
    }

    override val state = MutableStateFlow(
        State(
            period = initPeriod,
        )
    )

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
