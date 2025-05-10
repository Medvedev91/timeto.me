package me.timeto.shared.ui.repeatings.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.TextFeatures
import me.timeto.shared.UnixTime
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.launchExIo
import me.timeto.shared.misc.DaytimeUi
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.ui.UiException
import me.timeto.shared.vm.__Vm

class RepeatingFormVm(
    private val initRepeatingDb: RepeatingDb?,
) : __Vm<RepeatingFormVm.State>() {

    data class State(
        val title: String,
        val saveText: String,
        val text: String,
        val period: RepeatingDb.Period?,
        val daytimeUi: DaytimeUi?,
        val activityDb: ActivityDb?,
        val timerSeconds: Int?,
        val checklistsDb: List<ChecklistDb>,
        val shortcutsDb: List<ShortcutDb>,
        val isImportant: Boolean,
    ) {

        val textPlaceholder = "Task"

        val periodTitle = "Period"
        val periodNote: String = period?.title ?: "Not Selected"

        val daytimeHeader = "Time of the Day"
        val daytimeNote: String = daytimeUi?.text?.let { "at $it" } ?: "Not Selected"
        val daytimePickerUi: DaytimeUi = daytimeUi ?: DaytimeUi(hour = 12, minute = 0)

        val activityTitle = "Activity"
        val activitiesUi: List<ActivityUi> =
            Cache.activitiesDbSorted.map { ActivityUi(it) }

        val timerTitle = "Timer"
        val timerNote: String =
            timerSeconds?.toTimerHintNote(isShort = false) ?: "Not Selected"
        val timerPickerSeconds: Int = timerSeconds ?: (45 * 60)

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
                title = if (initRepeatingDb != null) "Edit Repeating" else "New Repeating",
                saveText = if (initRepeatingDb != null) "Save" else "Create",
                text = initRepeatingDb?.text ?: "",
                period = initRepeatingDb?.getPeriod(),
                daytimeUi = initRepeatingDb?.daytime?.let { DaytimeUi.byDaytime(it) },
                activityDb = tf.activity,
                timerSeconds = tf.timer,
                checklistsDb = tf.checklists,
                shortcutsDb = tf.shortcuts,
                isImportant = initRepeatingDb?.isImportant ?: false,
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

    fun setActivity(newActivityDb: ActivityDb?) {
        state.update { it.copy(activityDb = newActivityDb) }
    }

    fun setTimerSeconds(newTimerSeconds: Int) {
        state.update { it.copy(timerSeconds = newTimerSeconds) }
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

            val activityDb: ActivityDb =
                state.activityDb ?: throw UiException("Activity not selected")

            val timerSeconds: Int =
                state.timerSeconds ?: throw UiException("Timer not selected")

            val tf: TextFeatures = text.textFeatures().copy(
                activity = activityDb,
                timer = timerSeconds,
                checklists = state.checklistsDb,
                shortcuts = state.shortcutsDb,
            )

            val textTf: String = tf.textWithFeatures()

            val isImportant: Boolean =
                state.isImportant

            if (initRepeatingDb != null) {
                initRepeatingDb.updateWithValidationEx(
                    text = textTf,
                    period = period,
                    daytime = daytimeUi.seconds,
                    isImportant = isImportant,
                )
                TaskDb.getAsc().forEach { taskDb ->
                    val taskTf = taskDb.text.textFeatures()
                    if (taskTf.fromRepeating?.id == initRepeatingDb.id) {
                        val newTf = taskTf.copy(isImportant = isImportant)
                        taskDb.upTextWithValidation(newTf.textWithFeatures())
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
                )

                RepeatingDb.syncTodaySafe(RepeatingDb.todayWithOffset())
            }
            onUi {
                onSuccess()
            }
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }

    ///

    data class ActivityUi(
        val activityDb: ActivityDb,
    ) {
        val title: String =
            activityDb.name.textFeatures().textNoFeatures
    }
}
