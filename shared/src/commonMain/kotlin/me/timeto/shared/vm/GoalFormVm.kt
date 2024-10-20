package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.ColorRgba
import me.timeto.shared.TextFeatures
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote

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

        private val timer: Int? = textFeatures.timer
        val timerTitle = "Timer on Bar Pressed"
        val timerNote: String = timer?.toTimerHintNote(isShort = false) ?: "None"
        val timerNoteColor: ColorRgba? = if (timer == null) ColorRgba.red else null
        val timerPickerSheetTitle = "Timer"
        val timerDefaultMinutes: Int = (if (timer != null) (timer / 60) else 45)

        val finishedTitle = "Finished Emoji"
    }

    override val state = MutableStateFlow(
        State(
            id = initGoalFormUi?.id,
            textFeatures = (initGoalFormUi?.note ?: "").textFeatures(),
            finishedText = initGoalFormUi?.finishText ?: "👍",
        )
    )

    fun setNote(note: String) {
        state.update {
            val tf = it.textFeatures.copy(textNoFeatures = note)
            it.copy(textFeatures = tf)
        }
    }

    fun setTimer(timer: Int) {
        state.update {
            val tf = it.textFeatures.copy(timer = timer)
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
