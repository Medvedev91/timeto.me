package me.timeto.shared.ui.activities.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.ui.goals.form.GoalFormData
import me.timeto.shared.ui.goals.form.GoalFormStrategy
import me.timeto.shared.vm.__Vm

class ActivityFormGoalsVm(
    initGoalFormsData: List<GoalFormData>,
) : __Vm<ActivityFormGoalsVm.State>() {

    data class State(
        val goalFormsData: List<GoalFormData>,
    ) {
        val newGoalTitle = "New Goal"
    }

    override val state = MutableStateFlow(
        State(
            goalFormsData = initGoalFormsData,
        )
    )

    val newGoalStrategy = GoalFormStrategy.NewFormData(
        onDone = { newGoalFormData ->
            state.update {
                it.copy(goalFormsData = it.goalFormsData + newGoalFormData)
            }
        }
    )

    fun updateGoalFormData(idx: Int, new: GoalFormData) {
        state.update { state ->
            val newList = state.goalFormsData.toMutableList()
            newList[idx] = new
            state.copy(goalFormsData = newList)
        }
    }

    fun deleteGoalFormData(idx: Int) {
        state.update { state ->
            val newList = state.goalFormsData.toMutableList()
            newList.removeAt(idx)
            state.copy(goalFormsData = newList)
        }
    }
}
