package timeto.shared.vm.ui

import timeto.shared.ColorNative
import timeto.shared.TextFeatures
import timeto.shared.db.ActivityModel
import timeto.shared.db.ChecklistModel
import timeto.shared.db.ShortcutModel
import timeto.shared.textFeatures
import timeto.shared.toTimerHintNote

class TextFeaturesFormUI(
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

    ///

    fun upChecklists(checklists: List<ChecklistModel>) =
        textFeatures.copy(checklists = checklists)

    fun upShortcuts(shortcuts: List<ShortcutModel>) =
        textFeatures.copy(shortcuts = shortcuts)

    fun upActivity(activity: ActivityModel) =
        textFeatures.copy(activity = activity)

    fun upTimer(seconds: Int) =
        textFeatures.copy(timer = seconds)
}
