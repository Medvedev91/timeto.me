package timeto.shared.vm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timeto.shared.*
import timeto.shared.db.ActivityModel
import timeto.shared.db.ActivityModel__Data

class ActivityFormSheetVM(
    val activity: ActivityModel?
) : __VM<ActivityFormSheetVM.State>() {

    data class State(
        val headerTitle: String,
        val headerDoneText: String,
        val emoji: String?,
        val activityData: ActivityModel__Data,
        val textFeatures: TextFeatures,
        val isAutoFS: Boolean,
    ) {
        val inputNameValue = textFeatures.textNoFeatures
        val isHeaderDoneEnabled = (inputNameValue.isNotBlank() && emoji != null)
        val inputNameHeader = "ACTIVITY NAME"
        val inputNamePlaceholder = "Activity Name"
        val emojiTitle = "Unique Emoji"
        val emojiNotSelected = "Not Selected"
        val timerHintsHeader = "TIMER HINTS"
        val autoFSTitle = Strings.AUTO_FS_FORM_TITLE
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
            headerDoneText = if (activity != null) "Done" else "Create",
            emoji = activity?.emoji,
            activityData = activity?.getData() ?: ActivityModel__Data.buildDefault(),
            textFeatures = TextFeatures.parse(activity?.name ?: ""),
            isAutoFS = activity?.isAutoFs ?: false
        )
    )

    fun setInputNameValue(text: String) = state.update {
        it.copy(textFeatures = it.textFeatures.copy(textNoFeatures = text))
    }

    fun setEmoji(newEmoji: String) = state.update { it.copy(emoji = newEmoji) }

    fun setTriggers(newTriggers: List<Trigger>) = state.update {
        it.copy(textFeatures = it.textFeatures.copy(triggers = newTriggers))
    }

    fun toggleAutoFS() = state.update {
        it.copy(isAutoFS = !it.isAutoFS)
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

            val isAutoFS = state.value.isAutoFS

            if (activity != null) {
                activity.upByIdWithValidation(
                    name = nameWithFeatures,
                    emoji = selectedEmoji,
                    data = activityData,
                    isAutoFS = isAutoFS,
                )
            } else {
                ActivityModel.addWithValidation(
                    name = nameWithFeatures,
                    emoji = selectedEmoji,
                    deadline = 20 * 60,
                    sort = 0,
                    type = ActivityModel.TYPE.NORMAL,
                    colorRgba = ActivityModel.nextColor(),
                    data = activityData,
                    isAutoFS = isAutoFS,
                )
            }
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
