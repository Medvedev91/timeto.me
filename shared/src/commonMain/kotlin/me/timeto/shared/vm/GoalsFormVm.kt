package me.timeto.shared.vm

import kotlinx.coroutines.flow.*

class GoalsFormVm(
    initGoalFormsUi: List<ActivityFormSheetVm.GoalFormUi>,
) : __Vm<GoalsFormVm.State>() {

    data class State(
        val goalFormsUi: List<ActivityFormSheetVm.GoalFormUi>,
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
}
