package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.models.GoalFormUi

class GoalsFormVm(
    initGoalFormsUi: List<GoalFormUi>,
) : __Vm<GoalsFormVm.State>() {

    data class State(
        val goalFormsUi: List<GoalFormUi>,
    ) {

        val headerTitle = "Goals"
        val headerDoneText = "Done"

        val newGoalButtonText = "New Goal"
    }

    override val state = MutableStateFlow(
        State(
            goalFormsUi = initGoalFormsUi,
        )
    )

    fun addGoalFormUi(goalFormUi: GoalFormUi) {
        state.update { it.copy(goalFormsUi = it.goalFormsUi + goalFormUi) }
    }

    fun upGoalFormUi(
        idx: Int,
        goalFormUi: GoalFormUi,
    ) {
        state.update {
            val newGoalFormsUi = it.goalFormsUi.toMutableList()
            newGoalFormsUi[idx] = goalFormUi
            it.copy(goalFormsUi = newGoalFormsUi)
        }
    }
}
