package me.timeto.shared.vm.ui

import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ShortcutModel

class TextFeaturesTriggersFormUI(
    val textFeatures: TextFeatures,
) {

    val checklistsTitle = "Checklists"
    val checklistsNote = textFeatures.checklists.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: "None"

    val shortcutsTitle = "Shortcuts"
    val shortcutsNote = textFeatures.shortcuts.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: "None"

    fun setChecklists(checklists: List<ChecklistDb>) =
        textFeatures.copy(checklists = checklists)

    fun setShortcuts(shortcuts: List<ShortcutModel>) =
        textFeatures.copy(shortcuts = shortcuts)
}
