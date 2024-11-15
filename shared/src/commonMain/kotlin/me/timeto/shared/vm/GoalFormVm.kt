package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.GoalDb
import me.timeto.shared.models.GoalFormUi

class GoalFormVm(
    initGoalFormUi: GoalFormUi?,
) : __Vm<GoalFormVm.State>() {

    data class State(
        val isNew: Boolean,
        val seconds: Int,
        val period: GoalDb.Period?,
        val textFeatures: TextFeatures,
        val finishedText: String,
    ) {

        val headerTitle: String = if (isNew) "New Goal" else "Edit Goal"
        val headerDoneText = "Done"

        val periodTitle = "Period"
        val periodNote: String = period?.note() ?: "None"
        val periodNoteColor: ColorRgba? = if (period == null) ColorRgba.red else null

        val notePlaceholder = "Note (optional)"
        val note: String = textFeatures.textNoFeatures

        val durationTitle = "Duration"
        val durationNote: String = seconds.toTimerHintNote(isShort = false)
        val durationDefMinutes: Int = seconds / 60
        val durationPickerSheetTitle = "Target Duration"

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
            isNew = initGoalFormUi == null,
            seconds = initGoalFormUi?.seconds ?: (3 * 3_600),
            period = initGoalFormUi?.period,
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

    fun setPeriod(period: GoalDb.Period?) {
        state.update { it.copy(period = period) }
    }

    fun setDuration(seconds: Int) {
        state.update { it.copy(seconds = seconds) }
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

    fun setTextFeatures(tf: TextFeatures) {
        state.update { it.copy(textFeatures = tf) }
    }

    fun buildFormUi(
        onBuild: (GoalFormUi) -> Unit,
    ) {

        val stateValue = state.value
        val timer: Int? = stateValue.textFeatures.timer
        if (timer == null) {
            showUiAlert("Timer on bar pressed not selected")
            return
        }

        val period = stateValue.period
        if (period == null) {
            showUiAlert("Period not selected")
            return
        }

        val newGoalForm = GoalFormUi(
            seconds = stateValue.seconds,
            period = stateValue.period,
            note = stateValue.note,
            finishText = stateValue.finishedText
        )
        onBuild(newGoalForm)
    }
}
