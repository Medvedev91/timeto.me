package me.timeto.shared.ui.repeatings.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.launchExIo
import me.timeto.shared.misc.DaytimeUi
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.vm.__Vm

class RepeatingFormVm(
    initRepeatingDb: RepeatingDb?,
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

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ) {
        launchExIo {
            onUi {
                onSuccess()
            }
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
