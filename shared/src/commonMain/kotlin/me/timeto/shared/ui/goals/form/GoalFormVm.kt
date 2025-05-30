package me.timeto.shared.ui.goals.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.GoalDb
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.vm.__Vm

class GoalFormVm(
    strategy: GoalFormStrategy,
) : __Vm<GoalFormVm.State>() {

    data class State(
        val strategy: GoalFormStrategy,
        val note: String,
        val period: GoalDb.Period?,
        val seconds: Int,
        val timer: Int,
        val finishedText: String,
        val checklistsDb: List<ChecklistDb>,
        val shortcutsDb: List<ShortcutDb>,
    ) {

        val title: String = when (strategy) {
            is GoalFormStrategy.NewFormData -> "New Goal"
            is GoalFormStrategy.EditFormData -> "Edit Goal"
        }

        val doneText: String = when (strategy) {
            is GoalFormStrategy.NewFormData,
            is GoalFormStrategy.EditFormData -> "Done"
        }

        val notePlaceholder = "Note (optional)"

        val periodTitle = "Period"
        val periodNote: String = period?.note() ?: "None"

        val secondsTitle = "Duration"
        val secondsNote: String = seconds.toTimerHintNote(isShort = false)

        val timerTitle = "Timer on Bar Pressed"
        val timerNote: String = timer.toTimerHintNote(isShort = false)

        val finishedTextTitle = "Finished Emoji"

        val checklistsNote: String =
            if (checklistsDb.isEmpty()) "None"
            else checklistsDb.joinToString(", ") { it.name }

        val shortcutsNote: String =
            if (shortcutsDb.isEmpty()) "None"
            else shortcutsDb.joinToString(", ") { it.name }

        ///

        fun buildFormDataOrNull(
            dialogsManager: DialogsManager,
            goalDb: GoalDb?,
        ): GoalFormData? = try {
            validateData(goalDb = goalDb)
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
            null
        }

        @Throws(UiException::class)
        private fun validateData(
            goalDb: GoalDb?,
        ): GoalFormData {
            val noteValidated: String = note.trim()
            val tf: TextFeatures = noteValidated.textFeatures().copy(
                timer = timer,
                checklists = checklistsDb,
                shortcuts = shortcutsDb,
            )
            if (period == null)
                throw UiException("Period not selected")

            return GoalFormData(
                goalDb = goalDb,
                note = tf.textWithFeatures(),
                seconds = seconds,
                period = period,
                finishText = finishedText.trim(),
            )
        }
    }

    override val state: MutableStateFlow<State>

    init {
        val tf: TextFeatures
        val period: GoalDb.Period?
        val seconds: Int
        val finishedText: String
        when (strategy) {
            is GoalFormStrategy.NewFormData -> {
                tf = "".textFeatures()
                period = null
                seconds = 3 * 3_600
                finishedText = "👍"
            }
            is GoalFormStrategy.EditFormData -> {
                val formData: GoalFormData = strategy.initGoalFormData
                tf = formData.note.textFeatures()
                period = formData.period
                seconds = formData.seconds
                finishedText = formData.finishText
            }
        }
        state = MutableStateFlow(
            State(
                strategy = strategy,
                note = tf.textNoFeatures,
                period = period,
                seconds = seconds,
                timer = tf.timer ?: (45 * 60),
                finishedText = finishedText,
                checklistsDb = tf.checklists,
                shortcutsDb = tf.shortcuts,
            )
        )
    }

    ///

    fun setNote(newNote: String) {
        state.update { it.copy(note = newNote) }
    }

    fun setPeriod(newPeriod: GoalDb.Period) {
        state.update { it.copy(period = newPeriod) }
    }

    fun setSeconds(newSeconds: Int) {
        state.update { it.copy(seconds = newSeconds) }
    }

    fun setTimer(newTimer: Int) {
        state.update { it.copy(timer = newTimer) }
    }

    fun setFinishedText(newFinishedText: String) {
        state.update { it.copy(finishedText = newFinishedText) }
    }

    fun setChecklistsDb(newChecklistsDb: List<ChecklistDb>) {
        state.update { it.copy(checklistsDb = newChecklistsDb) }
    }

    fun setShortcutsDb(newShortcutsDb: List<ShortcutDb>) {
        state.update { it.copy(shortcutsDb = newShortcutsDb) }
    }
}
