package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.TextFeatures
import me.timeto.shared.textFeatures

class GoalFormVm(
    initGoalFormUi: ActivityFormSheetVm.GoalFormUi?,
) : __Vm<GoalFormVm.State>() {

    data class State(
        val id: Int?,
        val textFeatures: TextFeatures,
        val finishedText: String,
    ) {

        val headerTitle: String = if (id != null) "Edit Goal" else "New Goal"
        val headerDoneText = "Done"

        val notePlaceholder = "Note (optional)"
        val note: String = textFeatures.textNoFeatures

        val finishedTitle = "Finished Emoji"
    }

    override val state = MutableStateFlow(
        State(
            id = initGoalFormUi?.id,
            textFeatures = (initGoalFormUi?.note ?: "").textFeatures(),
            finishedText = initGoalFormUi?.finishText ?: "👍"
        )
    )

    fun setNote(note: String) {
        state.update {
            val tf = it.textFeatures.copy(textNoFeatures = note)
            it.copy(textFeatures = tf)
        }
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
