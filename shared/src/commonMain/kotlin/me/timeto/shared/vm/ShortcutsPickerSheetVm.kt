package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.Cache
import me.timeto.shared.db.ShortcutDb

class ShortcutsPickerSheetVm(
    selectedShortcuts: List<ShortcutDb>,
) : __Vm<ShortcutsPickerSheetVm.State>() {

    data class State(
        val shortcutsUI: List<ShortcutUI>,
    ) {
        val headerTitle = "Shortcuts"
        val doneTitle = "Done"
    }

    data class ShortcutUI(
        val shortcut: ShortcutDb,
        val isSelected: Boolean,
    ) {
        val text = shortcut.name
    }

    override val state = MutableStateFlow(
        State(
            shortcutsUI = Cache.shortcutsDb.map {
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
