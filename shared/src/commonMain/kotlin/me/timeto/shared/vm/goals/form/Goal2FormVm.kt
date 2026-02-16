package me.timeto.shared.vm.goals.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.ColorRgba
import me.timeto.shared.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.launchExIo
import me.timeto.shared.textFeatures
import me.timeto.shared.toHms
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.vm.Vm
import me.timeto.shared.vm.color_picker.ColorPickerExampleUi
import me.timeto.shared.vm.color_picker.ColorPickerExamplesUi

class Goal2FormVm(
    initGoalDb: Goal2Db?,
) : Vm<Goal2FormVm.State>() {

    data class State(
        val initGoalDb: Goal2Db?,
        val name: String,
        val seconds: Int,
        val secondsPickerItemsUi: List<SecondsPickerItemUi>,
        val parentGoalsUi: List<GoalUi>,
        val parentGoalUi: GoalUi?,
        val period: Goal2Db.Period,
        val timerTypeId: TimerTypeItemUi.TimerTypeUiId,
        val fixedTimer: Int,
        val colorRgba: ColorRgba,
        val keepScreenOn: Boolean,
        val pomodoroTimer: Int,
        val timerHints: List<Int>,
        val checklistsDb: List<ChecklistDb>,
        val shortcutsDb: List<ShortcutDb>,
    ) {

        val title: String =
            if (initGoalDb == null) "New Goal" else "Edit Goal"

        val doneText: String =
            if (initGoalDb == null) "Create" else "Save"
        val isDoneEnabled: Boolean =
            name.isNotBlank()

        val namePlaceholder = "Name"

        val secondsTitle = "Time"
        val secondsNote: String =
            secondsToString(seconds)

        val parentGoalTitle = "Parent Goal"

        val periodTitle = "Days"
        val periodNote: String =
            period.note()

        // region Timer

        val showFixedTimerPicker: Boolean =
            timerTypeId == TimerTypeItemUi.TimerTypeUiId.FixedTimer

        val timerTypeTitle = "Timer on Bar Pressed"
        val fixedTimerTitle = "Fixed Timer"
        val fixedTimerNote: String =
            fixedTimer.toTimerHintNote(isShort = false)

        val timerTypeItemsUi: List<TimerTypeItemUi> = listOf(
            TimerTypeItemUi(TimerTypeItemUi.TimerTypeUiId.RestOfGoal, "Rest of Goal"),
            TimerTypeItemUi(TimerTypeItemUi.TimerTypeUiId.FixedTimer, "Fixed Timer"),
            TimerTypeItemUi(TimerTypeItemUi.TimerTypeUiId.TimerPicker, "Timer Picker"),
        )

        // endregion

        val keepScreenOnTitle = "Keep Screen On"

        val pomodoroTitle = "Break Time"
        val pomodoroNote: String =
            PomodoroItemUi(pomodoroTimer).title
        val pomodoroItemsUi: List<PomodoroItemUi> =
            listOf(1, 2, 3, 4, 5, 10, 15, 30, 60).map { minutes ->
                PomodoroItemUi(timer = minutes * 60)
            }

        val timerHintsNote: String =
            if (timerHints.isEmpty()) "None"
            else timerHints.joinToString(", ") { it.toTimerHintNote(isShort = true) }

        val checklistsNote: String =
            if (checklistsDb.isEmpty()) "None"
            else checklistsDb.joinToString(", ") { it.name }

        val shortcutsNote: String =
            if (shortcutsDb.isEmpty()) "None"
            else shortcutsDb.joinToString(", ") { it.name }

        val colorTitle = "Color"
        val colorPickerTitle = "Goal Color"

        fun buildColorPickerExamplesUi() = ColorPickerExamplesUi(
            mainExampleUi = ColorPickerExampleUi(
                title = initGoalDb?.name?.textFeatures()?.textNoFeatures ?: "New Goal",
                colorRgba = colorRgba,
            ),
            secondaryHeader = "OTHER GOALS",
            secondaryExamplesUi = Cache.goals2Db
                .filter {
                    it.id != initGoalDb?.id
                }
                .map {
                    ColorPickerExampleUi(
                        title = it.name.textFeatures().textNoFeatures,
                        colorRgba = it.colorRgba,
                    )
                },
        )
    }

    override val state: MutableStateFlow<State>

    init {
        val tf = (initGoalDb?.name ?: "").textFeatures()
        val seconds: Int = initGoalDb?.seconds ?: 3_600
        val parentGoalsUi = Cache.goals2Db
            .filter { it.id != initGoalDb?.id }
            .map { GoalUi(it) }
        val parentGoalUi: GoalUi? =
            parentGoalsUi.firstOrNull { it.goalDb.id == initGoalDb?.parent_id }
        val timerType: Goal2Db.TimerType =
            initGoalDb?.buildTimerType() ?: Goal2Db.TimerType.RestOfGoal
        state = MutableStateFlow(
            State(
                initGoalDb = initGoalDb,
                name = tf.textNoFeatures,
                seconds = seconds,
                secondsPickerItemsUi = buildSecondsPickerItems(defSeconds = seconds),
                parentGoalsUi = parentGoalsUi,
                parentGoalUi = parentGoalUi,
                period = initGoalDb?.buildPeriod() ?: Goal2Db.Period.DaysOfWeek.everyDay,
                timerTypeId = when (timerType) {
                    Goal2Db.TimerType.RestOfGoal -> TimerTypeItemUi.TimerTypeUiId.RestOfGoal
                    Goal2Db.TimerType.TimerPicker -> TimerTypeItemUi.TimerTypeUiId.TimerPicker
                    is Goal2Db.TimerType.FixedTimer -> TimerTypeItemUi.TimerTypeUiId.FixedTimer
                },
                fixedTimer = when (timerType) {
                    is Goal2Db.TimerType.FixedTimer -> timerType.timer
                    else -> 45 * 60
                },
                colorRgba = initGoalDb?.colorRgba ?: Goal2Db.nextColorCached(),
                keepScreenOn = initGoalDb?.keepScreenOn ?: true,
                pomodoroTimer = initGoalDb?.pomodoro_timer ?: (5 * 60),
                timerHints = initGoalDb?.buildTimerHints() ?: emptyList(),
                checklistsDb = tf.checklistsDb,
                shortcutsDb = tf.shortcutsDb,
            )
        )
    }

    ///

    fun setName(newName: String) {
        state.update { it.copy(name = newName) }
    }

    fun setSeconds(newSeconds: Int) {
        state.update { it.copy(seconds = newSeconds) }
    }

    fun setParentGoalUi(goalUi: GoalUi?) {
        state.update { it.copy(parentGoalUi = goalUi) }
    }

    fun setPeriod(newPeriod: Goal2Db.Period) {
        state.update { it.copy(period = newPeriod) }
    }

    fun setTimerTypeId(newTimerTypeId: TimerTypeItemUi.TimerTypeUiId) {
        state.update { it.copy(timerTypeId = newTimerTypeId) }
    }

    fun setFixedTimer(newFixedTimer: Int) {
        state.update { it.copy(fixedTimer = newFixedTimer) }
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

    fun setTimerHints(newTimerHints: List<Int>) {
        state.update { it.copy(timerHints = newTimerHints) }
    }

    fun setChecklistsDb(newChecklistsDb: List<ChecklistDb>) {
        state.update { it.copy(checklistsDb = newChecklistsDb) }
    }

    fun setShortcutsDb(newShortcutsDb: List<ShortcutDb>) {
        state.update { it.copy(shortcutsDb = newShortcutsDb) }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: (Goal2Db) -> Unit,
    ): Unit = launchExIo {
        try {
            val state = state.value
            val initGoalDb: Goal2Db? = state.initGoalDb

            val nameWithFeatures: String = state.name.textFeatures().copy(
                checklistsDb = state.checklistsDb,
                shortcutsDb = state.shortcutsDb,
            ).textWithFeatures()

            val timerType: Goal2Db.TimerType = when (state.timerTypeId) {
                TimerTypeItemUi.TimerTypeUiId.RestOfGoal -> Goal2Db.TimerType.RestOfGoal
                TimerTypeItemUi.TimerTypeUiId.TimerPicker -> Goal2Db.TimerType.TimerPicker
                TimerTypeItemUi.TimerTypeUiId.FixedTimer -> Goal2Db.TimerType.FixedTimer(state.fixedTimer)
            }

            val newGoalDb: Goal2Db = if (initGoalDb != null) {
                initGoalDb.updateWithValidation(
                    name = nameWithFeatures,
                    seconds = state.seconds,
                    timerType = timerType,
                    period = state.period,
                    colorRgba = state.colorRgba,
                    keepScreenOn = state.keepScreenOn,
                    pomodoroTimer = state.pomodoroTimer,
                    timerHints = state.timerHints,
                    parentGoalDb = state.parentGoalUi?.goalDb,
                )
            } else {
                Goal2Db.insertWithValidation(
                    name = nameWithFeatures,
                    seconds = state.seconds,
                    timerType = timerType,
                    period = state.period,
                    colorRgba = state.colorRgba,
                    keepScreenOn = state.keepScreenOn,
                    pomodoroTimer = state.pomodoroTimer,
                    timerHints = state.timerHints,
                    parentGoalDb = state.parentGoalUi?.goalDb,
                    type = Goal2Db.Type.general,
                )
            }

            onUi {
                onSuccess(newGoalDb)
            }
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }

    fun delete(
        goalDb: Goal2Db,
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ) {
        val name: String =
            goalDb.name.textFeatures().textNoFeatures
        dialogsManager.confirmation(
            message = "Are you sure you want to delete \"$name\" goal?",
            buttonText = "Delete",
            onConfirm = {
                launchExIo {
                    try {
                        goalDb.deleteWithValidation()
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

    data class SecondsPickerItemUi(
        val title: String,
        val seconds: Int,
    )

    data class GoalUi(
        val goalDb: Goal2Db,
    ) {
        val title: String =
            goalDb.name.textFeatures().textNoFeatures
    }

    data class TimerTypeItemUi(
        val id: TimerTypeUiId,
        val title: String,
    ) {
        enum class TimerTypeUiId(val id: Int) {
            FixedTimer(0), RestOfGoal(1), TimerPicker(2),
        }
    }

    data class PomodoroItemUi(
        val timer: Int,
    ) {
        val title: String = when {
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
    }
}

private fun buildSecondsPickerItems(
    defSeconds: Int,
): List<Goal2FormVm.SecondsPickerItemUi> {

    val a: List<Int> =
        (1..10).map { it * 60 } + // 1 - 10 min by 1 min
                (1..10).map { (600 + (it * 300)) } + // 15 min - 1 hour by 5 min
                (1..138).map { (3_600 + (it * 600)) } + // 1 hour + by 10 min
                defSeconds

    return a.toSet().sorted().map { seconds ->
        Goal2FormVm.SecondsPickerItemUi(
            title = secondsToString(seconds),
            seconds = seconds,
        )
    }
}

private fun secondsToString(seconds: Int): String {
    val hours: Int = seconds / 3600
    val minutes: Int = (seconds % 3600) / 60
    return when {
        hours == 0 -> "$minutes min"
        minutes == 0 -> "$hours hr"
        else -> "$hours hr ${minutes.toString().padStart(2, '0')} min"
    }
}
