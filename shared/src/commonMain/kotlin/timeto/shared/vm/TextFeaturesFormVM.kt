package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.ColorNative
import timeto.shared.TextFeatures
import timeto.shared.db.ActivityModel
import timeto.shared.db.ChecklistModel
import timeto.shared.db.ShortcutModel
import timeto.shared.textFeatures
import timeto.shared.toTimerHintNote

class TextFeaturesFormVM(
    initTextFeatures: TextFeatures,
) : __VM<TextFeaturesFormVM.State>() {

    data class State(
        val textFeatures: TextFeatures,
    ) {

        val titleChecklist = "Checklists"
        val noteChecklists = textFeatures.checklists.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: "None"

        val titleShortcuts = "Shortcuts"
        val noteShortcuts = textFeatures.shortcuts.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: "None"

        val activityTitle = "Activity"
        val activityNote: String = run {
            val activity = textFeatures.activity ?: return@run "Not Selected"
            "${activity.name.textFeatures().textNoFeatures}  ${activity.emoji}"
        }
        val activityColorOrNull = if (textFeatures.activity == null) ColorNative.red else null

        val timerTitle = "Timer"
        val timerNote = textFeatures.timer?.toTimerHintNote(isShort = false) ?: "Not Selected"
        val timerColorOrNull = if (textFeatures.timer == null) ColorNative.red else null
    }

    override val state = MutableStateFlow(
        State(
            textFeatures = initTextFeatures,
        )
    )

    fun upChecklists(checklists: List<ChecklistModel>): TextFeatures {
        val newTextFeatures = state.value.textFeatures.copy(checklists = checklists)
        state.update { it.copy(textFeatures = newTextFeatures) }
        return newTextFeatures
    }

    fun upShortcuts(shortcuts: List<ShortcutModel>): TextFeatures {
        val newTextFeatures = state.value.textFeatures.copy(shortcuts = shortcuts)
        state.update { it.copy(textFeatures = newTextFeatures) }
        return newTextFeatures
    }

    fun upActivity(activity: ActivityModel): TextFeatures {
        val newTextFeatures = state.value.textFeatures.copy(activity = activity)
        state.update { it.copy(textFeatures = newTextFeatures) }
        return newTextFeatures
    }

    fun upTimer(seconds: Int): TextFeatures {
        val newTextFeatures = state.value.textFeatures.copy(timer = seconds)
        state.update { it.copy(textFeatures = newTextFeatures) }
        return newTextFeatures
    }
}
