package me.timeto.shared.vm

import kotlinx.coroutines.flow.*

class GoalFormVm(
    initGoalFormUi: ActivityFormSheetVm.GoalFormUi?,
) : __Vm<GoalFormVm.State>() {

    data class State(
        val id: Int?,
        val note: String,
        val finishedText: String,
    ) {

        val headerTitle: String = if (id != null) "Edit Goal" else "New Goal"
        val headerDoneText = "Done"

        val notePlaceholder = "Note (optional)"
        val finishedTitle = "Finished Emoji"
    }

    override val state = MutableStateFlow(
        State(
            id = initGoalFormUi?.id,
            note = initGoalFormUi?.note ?: "",
            finishedText = initGoalFormUi?.finishText ?: "👍"
        )
    )

    fun setNote(note: String) {
        state.update { it.copy(note = note) }
    }

    fun setFinishedText(text: String) {
        state.update { it.copy(finishedText = text) }
    }

    fun buildFormUi(
        onBuild: (ActivityFormSheetVm.GoalFormUi) -> Unit,
    ) {
        TODO()
    }
}
