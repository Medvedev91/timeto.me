package me.timeto.shared.vm.goals.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.GoalDb
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.launchExIo
import me.timeto.shared.vm.Vm

class GoalFormVm(
    strategy: GoalFormStrategy,
) : Vm<GoalFormVm.State>() {

    data class State(
        val strategy: GoalFormStrategy,
        val note: String,
        val isEntireActivity: Boolean,
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
            is GoalFormStrategy.NewGoal -> "New Goal"
            is GoalFormStrategy.EditGoal -> "Edit Goal"
        }

        val doneText: String = when (strategy) {
            is GoalFormStrategy.NewFormData -> "Done"
            is GoalFormStrategy.EditFormData -> "Done"
            is GoalFormStrategy.NewGoal -> "Create"
            is GoalFormStrategy.EditGoal -> "Save"
        }

        val notePlaceholder = "Note"

        val isEntireActivityNote = "Track Entire Activity"

        val periodTitle = "Period"
        val periodNote: String = period?.note() ?: "None"

        val secondsTitle = "Duration"
        val secondsNote: String = seconds.toTimerHintNote(isShort = false)

        val timerHeader = "Timer on Bar Pressed"
        val timerTitleRest = "Rest of Bar"
        val timerTitleTimer = "Timer"
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
                checklistsDb = checklistsDb,
                shortcutsDb = shortcutsDb,
            )
            if (tf.textNoFeatures.isBlank())
                throw UiException("Empty note")
            if (period == null)
                throw UiException("Period not selected")

            return GoalFormData(
                goalDb = goalDb,
                note = tf.textWithFeatures(),
                seconds = seconds,
                period = period,
                finishText = finishedText.trim(),
                isEntireActivity = isEntireActivity,
                timer = timer,
            )
        }
    }

    override val state: MutableStateFlow<State>

    init {
        val tf: TextFeatures
        val isEntireActivity: Boolean
        val period: GoalDb.Period?
        val seconds: Int
        val finishedText: String
        val timer: Int
        // Defaults
        val defaultFf: TextFeatures = "".textFeatures()
        val defaultIsEntireActivity = false
        val defaultPeriod: GoalDb.Period? = null
        val defaultSeconds = 3_600
        val defaultFinishedText = "👍"
        val defaultTimer = 0
        ///
        when (strategy) {
            is GoalFormStrategy.NewFormData -> {
                tf = defaultFf
                isEntireActivity = defaultIsEntireActivity
                period = defaultPeriod
                seconds = defaultSeconds
                finishedText = defaultFinishedText
                timer = defaultTimer
            }
            is GoalFormStrategy.EditFormData -> {
                val formData: GoalFormData = strategy.initGoalFormData
                tf = formData.note.textFeatures()
                isEntireActivity = formData.isEntireActivity
                period = formData.period
                seconds = formData.seconds
                finishedText = formData.finishText
                timer = formData.timer
            }
            is GoalFormStrategy.NewGoal -> {
                tf = defaultFf
                isEntireActivity = defaultIsEntireActivity
                period = defaultPeriod
                seconds = defaultSeconds
                finishedText = defaultFinishedText
                timer = defaultTimer
            }
            is GoalFormStrategy.EditGoal -> {
                val goalDb: GoalDb = strategy.goalDb
                tf = goalDb.note.textFeatures()
                isEntireActivity = goalDb.isEntireActivity
                period = goalDb.buildPeriod()
                seconds = goalDb.seconds
                finishedText = goalDb.finish_text
                timer = goalDb.timer
            }
        }
        state = MutableStateFlow(
            State(
                strategy = strategy,
                note = tf.textNoFeatures,
                isEntireActivity = isEntireActivity,
                period = period,
                seconds = seconds,
                timer = timer,
                finishedText = finishedText,
                checklistsDb = tf.checklistsDb,
                shortcutsDb = tf.shortcutsDb,
            )
        )
    }

    ///

    fun setNote(newNote: String) {
        state.update { it.copy(note = newNote) }
    }

    fun setIsEntireActivity(newIsEntireActivity: Boolean) {
        state.update { it.copy(isEntireActivity = newIsEntireActivity) }
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

    fun addGoal(
        activityDb: ActivityDb,
        dialogsManager: DialogsManager,
        onCreate: (GoalDb) -> Unit,
    ) {
        val formData: GoalFormData = state.value.buildFormDataOrNull(
            dialogsManager = dialogsManager,
            goalDb = null,
        ) ?: return
        launchExIo {
            val newGoalDb: GoalDb = GoalDb.insertAndGet(
                activityDb = activityDb,
                goalFormData = formData,
            )
            onUi {
                onCreate(newGoalDb)
            }
        }
    }

    fun saveGoal(
        goalDb: GoalDb,
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ) {
        val formData: GoalFormData = state.value.buildFormDataOrNull(
            dialogsManager = dialogsManager,
            goalDb = goalDb,
        ) ?: return
        launchExIo {
            goalDb.update(formData)
            onUi {
                onSuccess()
            }
        }
    }

    fun deleteGoal(goalDb: GoalDb) {
        launchExIo {
            goalDb.delete()
        }
    }
}
