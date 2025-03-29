package me.timeto.shared.ui.activities.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.ColorRgba
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.textFeatures
import me.timeto.shared.toHms
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.ui.UiException
import me.timeto.shared.ui.color.ColorPickerExampleData
import me.timeto.shared.ui.color.ColorPickerExamplesData
import me.timeto.shared.ui.goals.form.GoalFormData
import me.timeto.shared.vm.__Vm

class ActivityFormVm(
    initActivityDb: ActivityDb?,
) : __Vm<ActivityFormVm.State>() {

    data class State(
        val activityDb: ActivityDb?,
        val name: String,
        val emoji: String?,
        val colorRgba: ColorRgba,
        val keepScreenOn: Boolean,
        val pomodoroTimer: Int,
        val goalFormsData: List<GoalFormData>,
        val checklistsDb: List<ChecklistDb>,
        val shortcutsDb: List<ShortcutDb>,
    ) {

        val title: String =
            if (activityDb != null) "Edit Activity" else "New Activity"

        val saveText: String =
            if (activityDb != null) "Save" else "Create"

        val namePlaceholder = "Activity Name"

        val emojiTitle = "Unique Emoji"
        val emojiNotSelected = "Not Selected"

        val colorTitle = "Color"
        val colorPickerTitle = "Activity Color"

        val keepScreenOnTitle = "Keep Screen On"

        val pomodoroTitle = "Pomodoro"
        val pomodoroNote: String =
            prepPomodoroTimerString(pomodoroTimer)
        val pomodoroListItemsData: List<PomodoroListItemData> =
            pomodoroTimers.map { timer ->
                PomodoroListItemData(
                    timer = timer,
                    isSelected = pomodoroTimer == timer,
                )
            }

        val goalsTitle = "Goals"
        val goalsNote: String =
            if (goalFormsData.isEmpty()) "None"
            else goalFormsData.size.toString()

        val checklistsNote: String =
            if (checklistsDb.isEmpty()) "None"
            else checklistsDb.joinToString(", ") { it.name }

        val shortcutsNote: String =
            if (shortcutsDb.isEmpty()) "None"
            else shortcutsDb.joinToString(", ") { it.name }

        fun buildColorPickerExamplesData() = ColorPickerExamplesData(
            mainExample = ColorPickerExampleData(
                title = activityDb?.name ?: "New Activity",
                colorRgba = colorRgba,
            ),
            secondaryHeader = "OTHER ACTIVITIES",
            secondaryExamples = Cache.activitiesDbSorted
                .filter {
                    it.id != activityDb?.id
                }
                .map {
                    ColorPickerExampleData(
                        title = it.name,
                        colorRgba = it.colorRgba,
                    )
                },
        )
    }

    override val state: MutableStateFlow<State>

    init {

        val tf: TextFeatures =
            (initActivityDb?.name ?: "").textFeatures()

        state = MutableStateFlow(
            State(
                activityDb = initActivityDb,
                name = tf.textNoFeatures,
                emoji = initActivityDb?.emoji,
                colorRgba = initActivityDb?.colorRgba ?: ActivityDb.nextColorCached(),
                keepScreenOn = initActivityDb?.keepScreenOn ?: true,
                pomodoroTimer = initActivityDb?.pomodoro_timer ?: (5 * 60),
                goalFormsData = (initActivityDb?.getGoalsDbCached() ?: emptyList()).map {
                    GoalFormData.fromGoalDb(it)
                },
                checklistsDb = tf.checklists,
                shortcutsDb = tf.shortcuts,
            )
        )
    }

    ///

    fun setName(newName: String) {
        state.update { it.copy(name = newName) }
    }

    fun setEmoji(newEmoji: String) {
        state.update { it.copy(emoji = newEmoji) }
    }

    fun setColorRgba(newColorRgba: ColorRgba) {
        state.update { it.copy(colorRgba = newColorRgba) }
    }

    fun setKeepScreenOn(newKeepScreenOn: Boolean) {
        state.update { it.copy(keepScreenOn = newKeepScreenOn) }
    }

    fun setPomodoroTimer(newPomodoroTimer: Int) {
        state.update { it.copy(pomodoroTimer = newPomodoroTimer) }
    }

    fun setGoalFormsData(newGoalFormsData: List<GoalFormData>) {
        state.update { it.copy(goalFormsData = newGoalFormsData) }
    }

    fun setChecklistsDb(newChecklistsDb: List<ChecklistDb>) {
        state.update { it.copy(checklistsDb = newChecklistsDb) }
    }

    fun setShortcutsDb(newShortcutsDb: List<ShortcutDb>) {
        state.update { it.copy(shortcutsDb = newShortcutsDb) }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSave: () -> Unit,
    ) {
        TODO()
    }

    ///

    data class PomodoroListItemData(
        val timer: Int,
        val isSelected: Boolean,
    ) {
        val text: String =
            prepPomodoroTimerString(timer)
    }
}

///

private fun prepPomodoroTimerString(timer: Int): String = when {
    timer < 0 -> throw UiException("prepPomodoroTimerString(0)")
    timer < 3_600 -> "${timer / 60} min"
    else -> {
        val (h, m, _) = timer.toHms()
        if (m == 0)
            "$h ${if (h == 1) "hour" else "hours"}"
        else
            "${h}h ${m}m"
    }
}

private val pomodoroTimers: List<Int> =
    listOf(5, 10, 15, 30, 60).map { it * 60 }
