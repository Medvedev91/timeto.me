package me.timeto.shared.vm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.ActivityDb__Data
import me.timeto.shared.db.GoalDb

class ActivityFormSheetVm(
    val activity: ActivityDb?
) : __Vm<ActivityFormSheetVm.State>() {

    data class State(
        val headerTitle: String,
        val headerDoneText: String,
        val emoji: String?,
        val activityData: ActivityDb__Data,
        val goalsUi: List<GoalVmUi>,
        val textFeatures: TextFeatures,
        val keepScreenOn: Boolean,
        val colorRgba: ColorRgba,
        val pomodoroTimer: Int,
    ) {

        val inputNameValue = textFeatures.textNoFeatures
        val isHeaderDoneEnabled = (inputNameValue.isNotBlank() && emoji != null)
        val inputNameHeader = "ACTIVITY NAME"
        val inputNamePlaceholder = "Activity Name"
        val emojiTitle = "Unique Emoji"
        val emojiNotSelected = "Not Selected"
        val colorTitle = "Color"
        val timerHintsHeader = "TIMER HINTS"
        val keepScreenOnTitle = "Keep Screen On"

        val pomodoroTitle = "Pomodoro"
        val pomodoroNote: String = ActivityPomodoroSheetVm.prepPomodoroTimeString(pomodoroTimer)

        val goalsTitle = "Goals"
        val goalsNote: String = if (goalsUi.isEmpty()) "None" else goalsUi.size.toString()

        val deleteText = "Delete Activity"
        val timerHintsCustomItems = activityData.timer_hints.custom_list.map { seconds ->
            TimerHintCustomItem(seconds = seconds, text = seconds.toTimerHintNote(isShort = false))
        }
    }

    class GoalVmUi(
        val id: Int?, // null if new
        val seconds: Int,
        val period: GoalDb.Period,
        val note: String,
        val finishText: String,
    ) {

        companion object {

            fun fromGoalDb(goalDb: GoalDb) = GoalVmUi(
                id = null,
                seconds = goalDb.seconds,
                period = goalDb.buildPeriod(),
                note = goalDb.note,
                finishText = goalDb.finish_text,
            )
        }
    }

    data class TimerHintCustomItem(
        val seconds: Int,
        val text: String,
    )

    override val state = MutableStateFlow(
        State(
            headerTitle = if (activity != null) "Edit Activity" else "New Activity",
            headerDoneText = if (activity != null) "Save" else "Create",
            emoji = activity?.emoji,
            activityData = activity?.data ?: ActivityDb__Data.buildDefault(),
            goalsUi = activity?.getGoalsDbCached()?.map { GoalVmUi.fromGoalDb(it) } ?: emptyList(),
            textFeatures = (activity?.name ?: "").textFeatures(),
            keepScreenOn = activity?.keepScreenOn ?: true,
            colorRgba = activity?.colorRgba ?: ActivityDb.nextColorDI(),
            pomodoroTimer = activity?.pomodoro_timer ?: (5 * 60),
        )
    )

    fun setInputNameValue(text: String) = state.update {
        it.copy(textFeatures = it.textFeatures.copy(textNoFeatures = text))
    }

    fun setEmoji(newEmoji: String) = state.update { it.copy(emoji = newEmoji) }

    fun setTextFeatures(newTextFeatures: TextFeatures) = state.update {
        it.copy(textFeatures = newTextFeatures)
    }

    fun toggleKeepScreenOn() = state.update {
        it.copy(keepScreenOn = !it.keepScreenOn)
    }

    fun upColorRgba(colorRgba: ColorRgba) = state.update {
        it.copy(colorRgba = colorRgba)
    }

    fun setPomodoroTimer(pomodoroTimer: Int) = state.update {
        it.copy(pomodoroTimer = pomodoroTimer)
    }

    fun setGoals(goalsUi: List<GoalVmUi>): Unit = state.update {
        it.copy(goalsUi = goalsUi)
    }

    ///
    /// Timer Hints

    fun setTimerHintsType(type: ActivityDb__Data.TimerHints.HINT_TYPE) {
        setTimeHints(state.value.activityData.timer_hints.copy(type = type))
    }

    fun addCustomTimerHint(seconds: Int) {
        setTimeHints(
            state.value.activityData.timer_hints.copy(
                custom_list = (state.value.activityData.timer_hints.custom_list + seconds).distinct()
            )
        )
    }

    fun delCustomTimerHint(seconds: Int) {
        setTimeHints(
            state.value.activityData.timer_hints.copy(
                custom_list = state.value.activityData.timer_hints.custom_list
                    .toMutableList().apply {
                        remove(seconds)
                    }
            )
        )
    }

    private fun setTimeHints(newTimerHints: ActivityDb__Data.TimerHints) {
        state.update {
            it.copy(activityData = it.activityData.copy(timer_hints = newTimerHints))
        }
    }

    fun buildColorPickerInitData() = ActivityColorSheetVm.InitData(
        title = run {
            val emoji = state.value.emoji
            val title = state.value.inputNameValue
            if (emoji == null && title.isBlank())
                return@run "New Activity"
            return@run "${emoji ?: ""} $title".trim()
        },
        selectedColor = state.value.colorRgba,
    )

    //////

    fun save(
        onSuccess: () -> Unit
    ): Unit = scopeVm().launchEx {
        try {
            val selectedEmoji = state.value.emoji ?: return@launchEx showUiAlert("Emoji not selected")
            // todo check if a text without features
            val nameWithFeatures = state.value.textFeatures.textWithFeatures()

            val activityData = state.value.activityData
            activityData.assertValidity()

            val keepScreenOn = state.value.keepScreenOn
            val colorRgba = state.value.colorRgba

            val goals = state.value.goals

            val pomodoroTimer = state.value.pomodoroTimer

            if (activity != null) {
                activity.upByIdWithValidation(
                    name = nameWithFeatures,
                    emoji = selectedEmoji,
                    data = activityData,
                    keepScreenOn = keepScreenOn,
                    colorRgba = colorRgba,
                    goals = goals,
                    pomodoroTimer = pomodoroTimer,
                )
            } else {
                ActivityDb.addWithValidation(
                    name = nameWithFeatures,
                    emoji = selectedEmoji,
                    timer = 20 * 60,
                    sort = 0,
                    type = ActivityDb.TYPE.NORMAL,
                    colorRgba = colorRgba,
                    data = activityData,
                    keepScreenOn = keepScreenOn,
                    goals = goals,
                    pomodoroTimer = pomodoroTimer,
                )
            }
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }

    fun delete(
        onSuccess: () -> Unit
    ) {
        val activity = activity
        if (activity == null) {
            reportApi("ActivityFormSheetVM no activity. WTF??!!")
            return
        }

        val nameUi = activity.nameWithEmoji().textFeatures().textUi()
        showUiConfirmation(
            UIConfirmationData(
                text = "Are you sure you want to delete \"$nameUi\" activity?",
                buttonText = "Delete",
                isRed = true,
            ) {
                launchExDefault {
                    try {
                        activity.delete()
                        onSuccess()
                    } catch (e: UIException) {
                        showUiAlert(e.uiMessage)
                    }
                }
            }
        )
    }
}
