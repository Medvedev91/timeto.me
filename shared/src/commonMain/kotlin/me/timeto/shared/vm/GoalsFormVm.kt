package me.timeto.shared.vm

import kotlinx.coroutines.flow.*

class GoalsFormVm(
    initGoalsVmUi: List<ActivityFormSheetVm.GoalFormUi>,
) : __Vm<GoalsFormVm.State>() {

    data class State(
        val goalsVmUi: List<ActivityFormSheetVm.GoalFormUi>,
    ) {

        val headerTitle = "Goals"
        val headerDoneText = "Done"
    }

    override val state = MutableStateFlow(
        State(
            goalsVmUi = initGoalsVmUi,
        )
    )
}
