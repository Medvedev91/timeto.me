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
import me.timeto.shared.vm.__Vm

class GoalFormVm(
    private val strategy: GoalFormStrategy,
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
            is GoalFormStrategy.FormData ->
                if (strategy.initGoalFormData != null) "Edit Goal" else "New Goal"
        }

        val doneText: String = when (strategy) {
            is GoalFormStrategy.FormData -> "Done"
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
        ): GoalFormData? {
            val validatedData: ValidatedData = validateData(
                dialogsManager = dialogsManager,
            ) ?: return null
            TODO()
//            return GoalFormData(
//                goalDb = goalDb,
//                seconds =,
//                period =,
//                note =,
//                finishText =,
//            )
        }

        private fun validateData(
            dialogsManager: DialogsManager,
        ): ValidatedData? {
            val noteValidated: String = note.trim()
            val tf: TextFeatures = noteValidated.textFeatures()
            return ValidatedData(
                note = tf.textWithFeatures(),
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
            is GoalFormStrategy.FormData -> {
                val formData: GoalFormData? = strategy.initGoalFormData
                tf = (formData?.note ?: "").textFeatures()
                period = formData?.period
                seconds = formData?.seconds ?: (3 * 3_600)
                finishedText = formData?.finishText ?: "👍"
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

///

private data class ValidatedData(
    val note: String,
)
