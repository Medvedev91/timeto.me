package me.timeto.shared.vm

import kotlinx.coroutines.flow.*

class GoalFormVm(
    initGoalFormUi: ActivityFormSheetVm.GoalFormUi?,
) : __Vm<GoalFormVm.State>() {

    data class State(
        val id: Int?,
        val note: String,
    ) {

        val headerTitle: String = if (id != null) "Edit Goal" else "New Goal"
        val headerDoneText = "Done"

        val notePlaceholder = "Note (optional)"
    }

    override val state = MutableStateFlow(
        State(
            id = initGoalFormUi?.id,
            note = initGoalFormUi?.note ?: "",
        )
    )

    fun setNote(note: String) {
        state.update { it.copy(note = note) }
    }

    fun buildFormUi(
        onBuild: (ActivityFormSheetVm.GoalFormUi) -> Unit,
    ) {
        TODO()
    }
}
