package me.timeto.shared.vm.activities.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.ColorRgba
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.launchExIo
import me.timeto.shared.textFeatures
import me.timeto.shared.toHms
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.vm.color_picker.ColorPickerExampleUi
import me.timeto.shared.vm.color_picker.ColorPickerExamplesUi
import me.timeto.shared.vm.goals.form.GoalFormData
import me.timeto.shared.vm.Vm

class ActivityFormVm(
    initActivityDb: ActivityDb?,
) : Vm<ActivityFormVm.State>() {

    data class State(
        val initActivityDb: ActivityDb?,
        val name: String,
        val emoji: String?,
        val colorRgba: ColorRgba,
        val keepScreenOn: Boolean,
        val pomodoroTimer: Int,
        val goalFormsData: List<GoalFormData>,
        val timerHints: Set<Int>,
        val checklistsDb: List<ChecklistDb>,
        val shortcutsDb: List<ShortcutDb>,
    ) {

        val title: String =
            if (initActivityDb != null) "Edit Activity" else "New Activity"

        val doneText: String =
            if (initActivityDb != null) "Save" else "Create"

        val namePlaceholder = "Activity Name"

        val emojiTitle = "Unique Emoji"
        val emojiNotSelected = "Not Selected"

        val colorTitle = "Color"
        val colorPickerTitle = "Activity Color"

        val keepScreenOnTitle = "Keep Screen On"

        val pomodoroTitle = "Pomodoro"
        val pomodoroNote: String =
            prepPomodoroTimerString(pomodoroTimer)
        val pomodoroListItemsUi: List<PomodoroListItemUi> =
            pomodoroTimers.map { timer ->
                PomodoroListItemUi(
                    timer = timer,
                    isSelected = pomodoroTimer == timer,
                )
            }

        val goalsTitle = "Goals"
        val goalsNote: String =
            if (goalFormsData.isEmpty()) "None"
            else goalFormsData.size.toString()

        val timerHintsTitle = "Timer Hints"
        val timerHintsNote: String =
            if (timerHints.isEmpty()) "None"
            else timerHints.sorted().joinToString(", ") { seconds ->
                seconds.toTimerHintNote(isShort = true)
            }

        val checklistsNote: String =
            if (checklistsDb.isEmpty()) "None"
            else checklistsDb.joinToString(", ") { it.name }

        val shortcutsNote: String =
            if (shortcutsDb.isEmpty()) "None"
            else shortcutsDb.joinToString(", ") { it.name }

        fun buildColorPickerExamplesUi() = ColorPickerExamplesUi(
            mainExampleUi = ColorPickerExampleUi(
                title = initActivityDb?.name ?: "New Activity",
                colorRgba = colorRgba,
            ),
            secondaryHeader = "OTHER ACTIVITIES",
            secondaryExamplesUi = Cache.activitiesDbSorted
                .filter {
                    it.id != initActivityDb?.id
                }
                .map {
                    ColorPickerExampleUi(
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
                initActivityDb = initActivityDb,
                name = tf.textNoFeatures,
                emoji = initActivityDb?.emoji,
                colorRgba = initActivityDb?.colorRgba ?: ActivityDb.nextColorCached(),
                keepScreenOn = initActivityDb?.keepScreenOn ?: true,
                pomodoroTimer = initActivityDb?.pomodoro_timer ?: (5 * 60),
                goalFormsData = (initActivityDb?.getGoalsDbCached() ?: emptyList()).map {
                    GoalFormData.fromGoalDb(it)
                },
                timerHints = initActivityDb?.timerHints ?: emptySet(),
                checklistsDb = tf.checklistsDb,
                shortcutsDb = tf.shortcutsDb,
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

    fun setTimerHints(newTimerHints: Set<Int>) {
        state.update { it.copy(timerHints = newTimerHints) }
    }

    fun setChecklistsDb(newChecklistsDb: List<ChecklistDb>) {
        state.update { it.copy(checklistsDb = newChecklistsDb) }
    }

    fun setShortcutsDb(newShortcutsDb: List<ShortcutDb>) {
        state.update { it.copy(shortcutsDb = newShortcutsDb) }
    }

    ///

    fun save(
        dialogsManager: DialogsManager,
        onSave: () -> Unit,
    ): Unit = launchExIo {
        try {
            val state = state.value

            val emoji: String = state.emoji ?: throw UiException("Emoji not selected")
            val nameWithFeatures: String = state.name.textFeatures().copy(
                checklistsDb = state.checklistsDb,
                shortcutsDb = state.shortcutsDb,
            ).textWithFeatures()

            val activityDb: ActivityDb? = state.initActivityDb
            if (activityDb != null) {
                activityDb.upByIdWithValidation(
                    name = nameWithFeatures,
                    emoji = emoji,
                    keepScreenOn = state.keepScreenOn,
                    colorRgba = state.colorRgba,
                    goalFormsData = state.goalFormsData,
                    pomodoroTimer = state.pomodoroTimer,
                    timerHints = state.timerHints,
                )
            } else {
                ActivityDb.addWithValidation(
                    name = nameWithFeatures,
                    emoji = emoji,
                    timer = 20 * 60,
                    sort = 0,
                    type = ActivityDb.Type.general,
                    colorRgba = state.colorRgba,
                    keepScreenOn = state.keepScreenOn,
                    goalFormsData = state.goalFormsData,
                    pomodoroTimer = state.pomodoroTimer,
                    timerHints = state.timerHints,
                )
            }

            onUi {
                onSave()
            }
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }

    fun delete(
        activityDb: ActivityDb,
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ) {
        val name: String =
            activityDb.nameWithEmoji().textFeatures().textNoFeatures
        dialogsManager.confirmation(
            message = "Are you sure you want to delete \"$name\" activity?",
            buttonText = "Delete",
            onConfirm = {
                launchExIo {
                    try {
                        activityDb.delete()
                        onUi {
                            onSuccess()
                        }
                    } catch (e: UiException) {
                        dialogsManager.alert(e.uiMessage)
                    }
                }
            },
        )
    }

    ///

    data class PomodoroListItemUi(
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
    listOf(1, 2, 3, 4, 5, 10, 15, 30, 60).map { it * 60 }
