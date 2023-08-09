package me.timeto.shared.vm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.*
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.db.ActivityModel__Data

class ActivityFormSheetVM(
    val activity: ActivityModel?
) : __VM<ActivityFormSheetVM.State>() {

    data class State(
        val headerTitle: String,
        val headerDoneText: String,
        val emoji: String?,
        val activityData: ActivityModel__Data,
        val textFeatures: TextFeatures,
        val keepScreenOn: Boolean,
        val colorRgba: ColorRgba,
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
        val deleteText = "Delete Activity"
        val timerHintsCustomItems = activityData.timer_hints.custom_list.map { seconds ->
            TimerHintCustomItem(seconds = seconds, text = seconds.toTimerHintNote(isShort = false))
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
            activityData = activity?.getData() ?: ActivityModel__Data.buildDefault(),
            textFeatures = (activity?.name ?: "").textFeatures(),
            keepScreenOn = activity?.keepScreenOn ?: true,
            colorRgba = activity?.getColorRgba() ?: ActivityModel.nextColorDI(),
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

    ///
    /// Timer Hints

    fun setTimerHintsType(type: ActivityModel__Data.TimerHints.HINT_TYPE) {
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

    private fun setTimeHints(newTimerHints: ActivityModel__Data.TimerHints) {
        state.update {
            it.copy(activityData = it.activityData.copy(timer_hints = newTimerHints))
        }
    }

    fun buildColorPickerInitData() = ActivityColorSheetVM.InitData(
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
    ): Unit = scopeVM().launchEx {
        try {
            val selectedEmoji = state.value.emoji ?: return@launchEx showUiAlert("Emoji not selected")
            // todo check if a text without features
            val nameWithFeatures = state.value.textFeatures.textWithFeatures()

            val activityData = state.value.activityData
            activityData.assertValidity()

            val keepScreenOn = state.value.keepScreenOn
            val colorRgba = state.value.colorRgba

            if (activity != null) {
                activity.upByIdWithValidation(
                    name = nameWithFeatures,
                    emoji = selectedEmoji,
                    data = activityData,
                    keepScreenOn = keepScreenOn,
                    colorRgba = colorRgba,
                )
            } else {
                ActivityModel.addWithValidation(
                    name = nameWithFeatures,
                    emoji = selectedEmoji,
                    timer = 20 * 60,
                    sort = 0,
                    type = ActivityModel.TYPE.NORMAL,
                    colorRgba = colorRgba,
                    data = activityData,
                    keepScreenOn = keepScreenOn,
                )
            }
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }

    fun delete(
        onSuccess: () -> Unit
    ) = launchExDefault {
        try {
            val activity = activity
            if (activity == null) {
                reportApi("ActivityFormSheetVM no activity. WTF??!!")
                return@launchExDefault
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
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
