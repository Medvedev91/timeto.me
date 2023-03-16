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

        val checklistsTitle = "Checklists"
        val checklistsNote = textFeatures.checklists.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: "None"

        val shortcutsTitle = "Shortcuts"
        val shortcutsNote = textFeatures.shortcuts.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: "None"

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

    ///

    fun upChecklists(checklists: List<ChecklistModel>) =
        upTextFeatures(state.value.textFeatures.copy(checklists = checklists))

    fun upShortcuts(shortcuts: List<ShortcutModel>) =
        upTextFeatures(state.value.textFeatures.copy(shortcuts = shortcuts))

    fun upActivity(activity: ActivityModel) =
        upTextFeatures(state.value.textFeatures.copy(activity = activity))

    fun upTimer(seconds: Int) =
        upTextFeatures(state.value.textFeatures.copy(timer = seconds))

    private fun upTextFeatures(textFeatures: TextFeatures): TextFeatures {
        state.update { it.copy(textFeatures = textFeatures) }
        return textFeatures
    }
}
