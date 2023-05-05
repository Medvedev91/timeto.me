package timeto.shared.vm.ui

import timeto.shared.TextFeatures
import timeto.shared.db.ChecklistModel
import timeto.shared.db.ShortcutModel

class TextFeaturesTriggersFormUI(
    val textFeatures: TextFeatures,
) {

    val checklistsTitle = "Checklists"
    val checklistsNote = textFeatures.checklists.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: "None"

    val shortcutsTitle = "Shortcuts"
    val shortcutsNote = textFeatures.shortcuts.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: "None"

    fun setChecklists(checklists: List<ChecklistModel>) =
        textFeatures.copy(checklists = checklists)

    fun setShortcuts(shortcuts: List<ShortcutModel>) =
        textFeatures.copy(shortcuts = shortcuts)
}
