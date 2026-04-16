package me.timeto.shared.vm.activity_form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.ColorRgba
import me.timeto.shared.DaytimeUi
import me.timeto.shared.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.launchExIo
import me.timeto.shared.textFeatures
import me.timeto.shared.toHms
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.vm.Vm
import me.timeto.shared.vm.color_picker.ColorPickerExampleUi
import me.timeto.shared.vm.color_picker.ColorPickerExamplesUi

class ActivityFormVm(
    initActivityDb: ActivityDb?,
) : Vm<ActivityFormVm.State>() {

    data class State(
        val initActivityDb: ActivityDb?,
        val name: String,
        // region Goal
        val goalTypeUi: GoalTypeUi?,
        val goalTimerSeconds: Int,
        val goalCounterCount: Int,
        // endregion
        val parentActivitiesUi: List<ActivityUi>,
        val parentActivityUi: ActivityUi?,
        val period: ActivityDb.Period,
        val timerTypeUi: TimerTypeUi,
        val fixedTimer: Int,
        val timerDaytimeUi: DaytimeUi,
        val colorRgba: ColorRgba,
        val keepScreenOn: Boolean,
        val pomodoroTimer: Int,
        val timerHints: List<Int>,
        val checklistsDb: List<ChecklistDb>,
        val shortcutsDb: List<ShortcutDb>,
    ) {

        val title: String =
            if (initActivityDb == null) "New Activity" else "Edit Activity"

        val doneText: String =
            if (initActivityDb == null) "Create" else "Save"
        val isDoneEnabled: Boolean =
            name.isNotBlank()

        val namePlaceholder = "Name"

        // region Goal

        val goalHeader = "GOAL"
        val goalTypeTitle = "Goal"
        val goalTypeNote: String = goalTypeUi?.title ?: "None"

        val goalTimerTitle: String = GoalTypeUi.Timer.title
        val goalTimerNote: String = secondsToString(goalTimerSeconds)

        val goalCounterTitle: String = GoalTypeUi.Counter.title
        val goalCounterNote: String = "$goalCounterCount"

        val goalTypesUi: List<GoalTypeUi> =
            GoalTypeUi.entries.toList()

        val goalCountItemsUi: List<GoalCountUi> =
            (1..100).map { count -> GoalCountUi(count = count) }

        // endregion

        val parentActivityTitle = "Parent Activity"

        val periodTitle = "Days"
        val periodNote: String =
            period.note()

        // region Timer

        val timerTypeTitle = "Timer on Bar Pressed"

        val showFixedTimerPicker: Boolean =
            timerTypeUi == TimerTypeUi.FixedTimer
        val fixedTimerTitle = "Fixed Timer"
        val fixedTimerNote: String =
            fixedTimer.toTimerHintNote(isShort = false)

        val showDaytimeTimerPicker: Boolean =
            timerTypeUi == TimerTypeUi.Daytime
        val daytimeTimerTitle = "Time of Day"
        val daytimeTimerNote: String =
            timerDaytimeUi.text

        val timerTypesUi: List<TimerTypeUi> =
            TimerTypeUi.entries.toList()

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
        val colorPickerTitle = "Activity Color"

        fun buildColorPickerExamplesUi() = ColorPickerExamplesUi(
            mainExampleUi = ColorPickerExampleUi(
                title = initActivityDb?.name?.textFeatures()?.textNoFeatures ?: "New Activity",
                colorRgba = colorRgba,
            ),
            secondaryHeader = "OTHER ACTIVITIES",
            secondaryExamplesUi = Cache.activitiesDb
                .filter {
                    it.id != initActivityDb?.id
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
        val tf = (initActivityDb?.name ?: "").textFeatures()

        val parentActivitiesUi = Cache.activitiesDb
            .filter { it.id != initActivityDb?.id }
            .map { ActivityUi(it) }
        val parentActivityUi: ActivityUi? =
            parentActivitiesUi.firstOrNull { it.activityDb.id == initActivityDb?.parent_id }
        val timerType: ActivityDb.TimerType =
            initActivityDb?.buildTimerType() ?: ActivityDb.TimerType.StopwatchDaily

        val goalType: ActivityDb.GoalType? =
            initActivityDb?.buildGoalTypeOrNull()

        state = MutableStateFlow(
            State(
                initActivityDb = initActivityDb,
                name = tf.textNoFeatures,
                // region Goal
                goalTypeUi = when (goalType) {
                    is ActivityDb.GoalType.Timer -> GoalTypeUi.Timer
                    is ActivityDb.GoalType.Counter -> GoalTypeUi.Counter
                    ActivityDb.GoalType.Checklist -> GoalTypeUi.Checklist
                    null -> null
                },
                goalTimerSeconds = (goalType as? ActivityDb.GoalType.Timer)?.seconds ?: 3_600,
                goalCounterCount = (goalType as? ActivityDb.GoalType.Counter)?.count ?: 1,
                // endregion
                parentActivitiesUi = parentActivitiesUi,
                parentActivityUi = parentActivityUi,
                period = initActivityDb?.buildPeriod() ?: ActivityDb.Period.DaysOfWeek.everyDay,
                timerTypeUi = when (timerType) {
                    ActivityDb.TimerType.RestOfGoal -> TimerTypeUi.RestOfGoal
                    ActivityDb.TimerType.TimerPicker -> TimerTypeUi.TimerPicker
                    ActivityDb.TimerType.StopwatchZero -> TimerTypeUi.StopwatchZero
                    ActivityDb.TimerType.StopwatchDaily -> TimerTypeUi.StopwatchDaily
                    is ActivityDb.TimerType.FixedTimer -> TimerTypeUi.FixedTimer
                    is ActivityDb.TimerType.Daytime -> TimerTypeUi.Daytime
                },
                fixedTimer = when (timerType) {
                    is ActivityDb.TimerType.FixedTimer -> timerType.timer
                    else -> 45 * 60
                },
                timerDaytimeUi = when (timerType) {
                    is ActivityDb.TimerType.Daytime -> timerType.dayTimeUi
                    else -> DaytimeUi(hour = 12, minute = 0)
                },
                colorRgba = initActivityDb?.colorRgba ?: ActivityDb.nextColorCached(),
                keepScreenOn = initActivityDb?.keepScreenOn ?: true,
                pomodoroTimer = initActivityDb?.pomodoro_timer ?: (5 * 60),
                timerHints = initActivityDb?.buildTimerHints() ?: emptyList(),
                checklistsDb = tf.checklistsDb,
                shortcutsDb = tf.shortcutsDb,
            )
        )
    }

    ///

    fun setName(newName: String) {
        state.update { it.copy(name = newName) }
    }

    fun setGoalType(goalTypeUi: GoalTypeUi?) {
        state.update { it.copy(goalTypeUi = goalTypeUi) }
    }

    fun setGoalTimer(seconds: Int) {
        state.update { it.copy(goalTimerSeconds = seconds) }
    }

    fun setGoalCounter(count: Int) {
        state.update { it.copy(goalCounterCount = count) }
    }

    fun setParentActivityUi(activityUi: ActivityUi?) {
        state.update { it.copy(parentActivityUi = activityUi) }
    }

    fun setPeriod(newPeriod: ActivityDb.Period) {
        state.update { it.copy(period = newPeriod) }
    }

    fun setTimerType(timerTypeUi: TimerTypeUi) {
        state.update { it.copy(timerTypeUi = timerTypeUi) }
    }

    fun setFixedTimer(newFixedTimer: Int) {
        state.update { it.copy(fixedTimer = newFixedTimer) }
    }

    fun setDaytimeTimer(newDaytimeUi: DaytimeUi) {
        state.update { it.copy(timerDaytimeUi = newDaytimeUi) }
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
        onSuccess: (ActivityDb) -> Unit,
    ): Unit = launchExIo {
        try {
            val state = state.value
            val initActivityDb: ActivityDb? = state.initActivityDb

            val goalType: ActivityDb.GoalType? = when (state.goalTypeUi) {
                GoalTypeUi.Timer -> ActivityDb.GoalType.Timer(seconds = state.goalTimerSeconds)
                GoalTypeUi.Counter -> ActivityDb.GoalType.Counter(count = state.goalCounterCount)
                GoalTypeUi.Checklist -> ActivityDb.GoalType.Checklist
                null -> null
            }

            if (goalType == ActivityDb.GoalType.Checklist && state.checklistsDb.isEmpty())
                throw UiException("No Checklist Selected")

            val nameWithFeatures: String = state.name.textFeatures().copy(
                checklistsDb = state.checklistsDb,
                shortcutsDb = state.shortcutsDb,
            ).textWithFeatures()

            val timerType: ActivityDb.TimerType = when (state.timerTypeUi) {
                TimerTypeUi.RestOfGoal -> ActivityDb.TimerType.RestOfGoal
                TimerTypeUi.TimerPicker -> ActivityDb.TimerType.TimerPicker
                TimerTypeUi.StopwatchZero -> ActivityDb.TimerType.StopwatchZero
                TimerTypeUi.StopwatchDaily -> ActivityDb.TimerType.StopwatchDaily
                TimerTypeUi.FixedTimer -> ActivityDb.TimerType.FixedTimer(state.fixedTimer)
                TimerTypeUi.Daytime -> ActivityDb.TimerType.Daytime(state.timerDaytimeUi)
            }

            val newActivityDb: ActivityDb = if (initActivityDb != null) {
                initActivityDb.updateWithValidation(
                    name = nameWithFeatures,
                    goalType = goalType,
                    timerType = timerType,
                    period = state.period,
                    emoji = initActivityDb.emoji,
                    colorRgba = state.colorRgba,
                    keepScreenOn = state.keepScreenOn,
                    pomodoroTimer = state.pomodoroTimer,
                    timerHints = state.timerHints,
                    parentActivityDb = state.parentActivityUi?.activityDb,
                )
            } else {
                ActivityDb.insertWithValidation(
                    name = nameWithFeatures,
                    goalType = goalType,
                    timerType = timerType,
                    period = state.period,
                    emoji = "❔",
                    colorRgba = state.colorRgba,
                    keepScreenOn = state.keepScreenOn,
                    pomodoroTimer = state.pomodoroTimer,
                    timerHints = state.timerHints,
                    parentActivityDb = state.parentActivityUi?.activityDb,
                    type = ActivityDb.Type.general,
                )
            }

            onUi {
                onSuccess(newActivityDb)
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
            activityDb.name.textFeatures().textNoFeatures
        dialogsManager.confirmation(
            message = "Are you sure you want to delete \"$name\" activity?",
            buttonText = "Delete",
            onConfirm = {
                launchExIo {
                    try {
                        activityDb.deleteWithValidation()
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

    enum class GoalTypeUi(val id: Int, val title: String) {
        Timer(1, "Amount of Time"),
        Counter(2, "Number of Times"),
        Checklist(3, "Complete Checklist"),
    }

    data class GoalCountUi(
        val count: Int,
    ) {
        val title: String = "$count"
    }

    data class ActivityUi(
        val activityDb: ActivityDb,
    ) {
        val title: String =
            activityDb.name.textFeatures().textNoFeatures
    }

    enum class TimerTypeUi(val id: Int, val title: String) {
        RestOfGoal(1, "Rest of Goal"),
        FixedTimer(2, "Fixed Timer"),
        TimerPicker(3, "Timer Picker"),
        Daytime(4, "Time of Day"),
        StopwatchZero(5, "Stopwatch"),
        StopwatchDaily(6, "Daily Stopwatch"),
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

private fun secondsToString(seconds: Int): String {
    val hours: Int = seconds / 3600
    val minutes: Int = (seconds % 3600) / 60
    return when {
        hours == 0 -> "$minutes min"
        minutes == 0 -> "$hours hr"
        else -> "$hours hr ${minutes.toString().padStart(2, '0')} min"
    }
}
