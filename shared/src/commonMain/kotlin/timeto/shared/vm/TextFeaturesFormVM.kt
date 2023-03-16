package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.TextFeatures
import timeto.shared.db.ChecklistModel
import timeto.shared.db.ShortcutModel

class TextFeaturesFormVM(
    initTextFeatures: TextFeatures,
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
            textFeatures = initTextFeatures,
        )
    )

    fun upChecklists(checklists: List<ChecklistModel>) : TextFeatures {
        val newTextFeatures = state.value.textFeatures.copy(checklists = checklists)
        state.update { it.copy(textFeatures = newTextFeatures) }
        return newTextFeatures
    }

    fun upShortcuts(shortcuts: List<ShortcutModel>) : TextFeatures {
        val newTextFeatures = state.value.textFeatures.copy(shortcuts = shortcuts)
        state.update { it.copy(textFeatures = newTextFeatures) }
        return newTextFeatures
    }
}
