package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.TextFeatures
import timeto.shared.db.ChecklistModel
import timeto.shared.db.ShortcutModel

class TextFeaturesFormVM(
    val textFeatures: TextFeatures,
) : __VM<TextFeaturesFormVM.State>() {

    data class State(
        val textFeatures: TextFeatures,
    ) {

        val titleChecklist = "Checklists"
        val titleShortcuts = "Shortcuts"

        val noteChecklists = textFeatures.checklists.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: "None"
        val noteShortcuts = textFeatures.shortcuts.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: "None"
    }

    override val state = MutableStateFlow(
        State(
            textFeatures = textFeatures,
        )
    )

    fun upChecklists(checklists: List<ChecklistModel>) =
        textFeatures.copy(checklists = checklists)

    fun upShortcuts(shortcuts: List<ShortcutModel>) =
        textFeatures.copy(shortcuts = shortcuts)
}
