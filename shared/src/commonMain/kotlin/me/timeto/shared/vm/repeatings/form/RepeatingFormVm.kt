package me.timeto.shared.vm.repeatings.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.DayStartOffsetUtils
import me.timeto.shared.TextFeatures
import me.timeto.shared.UnixTime
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.launchExIo
import me.timeto.shared.DaytimeUi
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.vm.Vm

class RepeatingFormVm(
    initRepeatingDb: RepeatingDb?,
) : Vm<RepeatingFormVm.State>() {

    data class State(
        val initRepeatingDb: RepeatingDb?,
        val title: String,
        val doneText: String,
        val text: String,
        val period: RepeatingDb.Period?,
        val daytimeUi: DaytimeUi?,
        val timerSeconds: Int?,
        val goalDb: Goal2Db?,
        val checklistsDb: List<ChecklistDb>,
        val shortcutsDb: List<ShortcutDb>,
        val isImportant: Boolean,
        val inCalendar: Boolean,
    ) {

        val textPlaceholder = "Task"

        val periodTitle = "Period"
        val periodNote: String = period?.title ?: "Not Selected"

        val daytimeTitle = "Time of the Day"
        val daytimeNote: String = daytimeUi?.text?.let { "at $it" } ?: "Not Selected"
        val daytimePickerUi: DaytimeUi = daytimeUi ?: DaytimeUi(hour = 12, minute = 0)

        val timerTitle = "Timer"
        val timerNote: String =
            timerSeconds?.toTimerHintNote(isShort = false) ?: "Not Selected"
        val timerPickerSeconds: Int = timerSeconds ?: (45 * 60)

        val goalTitle = "Goal"
        val goalNote: String =
            goalDb?.name?.textFeatures()?.textNoFeatures ?: "Not Selected"
        val goalsUi: List<GoalUi> =
            Cache.goals2Db.map { GoalUi(it) }

        val checklistsTitle = "Checklists"
        val checklistsNote: String =
            checklistsDb.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: "None"

        val shortcutsTitle = "Shortcuts"
        val shortcutsNote: String =
            shortcutsDb.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: "None"

        val isImportantTitle = "Is Important"
    }

    override val state: MutableStateFlow<State>

    init {
        val tf: TextFeatures = (initRepeatingDb?.text ?: "").textFeatures()
        state = MutableStateFlow(
            State(
                initRepeatingDb = initRepeatingDb,
                title = if (initRepeatingDb != null) "Edit Repeating" else "New Repeating",
                doneText = if (initRepeatingDb != null) "Save" else "Create",
                text = tf.textNoFeatures,
                period = initRepeatingDb?.getPeriod(),
                daytimeUi = initRepeatingDb?.daytime?.let { DaytimeUi.byDaytime(it) },
                timerSeconds = tf.timer,
                goalDb = tf.goalDb,
                checklistsDb = tf.checklistsDb,
                shortcutsDb = tf.shortcutsDb,
                isImportant = initRepeatingDb?.isImportant ?: false,
                inCalendar = initRepeatingDb?.inCalendar ?: true,
            )
        )
    }

    fun setText(newText: String) {
        state.update { it.copy(text = newText) }
    }

    fun setPeriod(newPeriod: RepeatingDb.Period) {
        state.update { it.copy(period = newPeriod) }
    }

    fun setDaytime(newDaytimeUi: DaytimeUi?) {
        state.update { it.copy(daytimeUi = newDaytimeUi) }
    }

    fun setTimerSeconds(newTimerSeconds: Int) {
        state.update { it.copy(timerSeconds = newTimerSeconds) }
    }

    fun setGoal(newGoalDb: Goal2Db?) {
        state.update { it.copy(goalDb = newGoalDb) }
    }

    fun setChecklists(newChecklistsDb: List<ChecklistDb>) {
        state.update { it.copy(checklistsDb = newChecklistsDb) }
    }

    fun setShortcuts(newShortcutsDb: List<ShortcutDb>) {
        state.update { it.copy(shortcutsDb = newShortcutsDb) }
    }

    fun setIsImportant(newIsImportant: Boolean) {
        state.update { it.copy(isImportant = newIsImportant) }
    }

    fun setInCalendar(newInCalendar: Boolean) {
        state.update { it.copy(inCalendar = newInCalendar) }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ): Unit = launchExIo {
        try {
            val state: State = state.value

            val text: String = state.text.trim()
            if (text.isBlank())
                throw UiException("No text")

            val period: RepeatingDb.Period =
                state.period ?: throw UiException("Period not selected")

            val daytimeUi: DaytimeUi =
                state.daytimeUi ?: throw UiException("Time of the day is not selected")

            val goalDb: Goal2Db =
                state.goalDb ?: throw UiException("Goal not selected")

            val timerSeconds: Int =
                state.timerSeconds ?: throw UiException("Timer not selected")

            val tf: TextFeatures = text.textFeatures().copy(
                timer = timerSeconds,
                goalDb = goalDb,
                checklistsDb = state.checklistsDb,
                shortcutsDb = state.shortcutsDb,
            )

            val textTf: String = tf.textWithFeatures()

            val isImportant: Boolean =
                state.isImportant

            val repeatingDb: RepeatingDb? = state.initRepeatingDb
            if (repeatingDb != null) {
                repeatingDb.updateWithValidationEx(
                    text = textTf,
                    period = period,
                    daytime = daytimeUi.seconds,
                    isImportant = isImportant,
                    inCalendar = state.inCalendar,
                )
                TaskDb.selectAsc().forEach { taskDb ->
                    val taskTf = taskDb.text.textFeatures()
                    if (taskTf.fromRepeating?.id == repeatingDb.id) {
                        val newTf = taskTf.copy(isImportant = isImportant)
                        taskDb.updateTextWithValidation(newTf.textWithFeatures())
                    }
                }
            } else {
                val lastDay: Int = if (period is RepeatingDb.Period.EveryNDays && period.nDays == 1)
                    UnixTime().localDay - 1
                else
                    UnixTime().localDay

                RepeatingDb.insertWithValidationEx(
                    text = textTf,
                    period = period,
                    lastDay = lastDay,
                    daytime = daytimeUi.seconds,
                    isImportant = isImportant,
                    inCalendar = state.inCalendar,
                )

                RepeatingDb.syncTodaySafe(DayStartOffsetUtils.getToday())
            }
            onUi {
                onSuccess()
            }
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }

    fun delete(
        repeatingDb: RepeatingDb,
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ) {
        val name: String =
            repeatingDb.text.textFeatures().textNoFeatures
        dialogsManager.confirmation(
            message = "Are you sure you want to delete \"$name\" repeating task?",
            buttonText = "Delete",
            onConfirm = {
                launchExIo {
                    repeatingDb.delete()
                    onUi {
                        onSuccess()
                    }
                }
            },
        )
    }

    ///

    data class GoalUi(
        val goalDb: Goal2Db,
    ) {
        val title: String =
            goalDb.name.textFeatures().textNoFeatures
    }
}
