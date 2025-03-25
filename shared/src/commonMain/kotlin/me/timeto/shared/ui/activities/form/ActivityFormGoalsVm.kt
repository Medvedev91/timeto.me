package me.timeto.shared.ui.activities.form

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.ui.goals.GoalFormData
import me.timeto.shared.vm.__Vm

class ActivityFormGoalsVm(
    initGoalsFormData: List<GoalFormData>,
) : __Vm<ActivityFormGoalsVm.State>() {

    data class State(
        val goalsFormData: List<GoalFormData>,
    )

    override val state = MutableStateFlow(
        State(
            goalsFormData = initGoalsFormData,
        )
    )
}
