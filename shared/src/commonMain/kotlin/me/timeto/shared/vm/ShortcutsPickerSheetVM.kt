package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.DI
import me.timeto.shared.db.ShortcutModel

class ShortcutsPickerSheetVM(
    selectedShortcuts: List<ShortcutModel>,
) : __VM<ShortcutsPickerSheetVM.State>() {

    data class State(
        val shortcutsUI: List<ShortcutUI>,
    ) {
        val headerTitle = "Shortcuts"
        val doneTitle = "Done"
    }

    data class ShortcutUI(
        val shortcut: ShortcutModel,
        val isSelected: Boolean,
    ) {
        val text = shortcut.name
    }

    override val state = MutableStateFlow(
        State(
            shortcutsUI = DI.shortcuts.map {
                ShortcutUI(it, it.id in selectedShortcuts.map { it.id })
            },
        )
    )

    fun toggleShortcut(shortcutUI: ShortcutUI) {
        val shortcutsUI = state.value.shortcutsUI.toMutableList()
        val idx = shortcutsUI.indexOf(shortcutUI) // todo report if -1
        shortcutsUI[idx] = shortcutUI.copy(isSelected = !shortcutUI.isSelected)
        state.update { it.copy(shortcutsUI = shortcutsUI) }
    }

    fun getSelectedShortcuts() = state.value.shortcutsUI
        .filter { it.isSelected }
        .map { it.shortcut }
}
