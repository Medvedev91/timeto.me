package me.timeto.shared.vm.activities.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.vm.goals.form.GoalFormData
import me.timeto.shared.vm.goals.form.GoalFormStrategy
import me.timeto.shared.vm.Vm

class ActivityFormGoalsVm(
    initGoalFormsData: List<GoalFormData>,
) : Vm<ActivityFormGoalsVm.State>() {

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
        },
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
