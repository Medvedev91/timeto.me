package me.timeto.shared.vm.shortcuts

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.onEachExIn
import me.timeto.shared.vm.__Vm

class ShortcutsPickerVm(
    initShortcutsDb: List<ShortcutDb>,
) : __Vm<ShortcutsPickerVm.State>() {

    data class State(
        val shortcutsDb: List<ShortcutDb>,
        val selectedIds: Set<Int>,
    ) {

        val title = "Shortcuts"
        val doneText = "Done"

        val shortcutsDbSorted: List<ShortcutDb> =
            shortcutsDb.sortedByDescending { it.id in selectedIds }
    }

    override val state = MutableStateFlow(
        State(
            shortcutsDb = initShortcutsDb,
            selectedIds = initShortcutsDb.map { it.id }.toSet(),
        )
    )

    init {
        val scopeVm = scopeVm()
        ShortcutDb.selectAscFlow().onEachExIn(scopeVm) { shortcutsDb ->
            state.update { it.copy(shortcutsDb = shortcutsDb) }
        }
    }

    fun toggleShortcut(shortcutDb: ShortcutDb) {
        val newSelectedIds = state.value.selectedIds.toMutableSet()
        val shortcutId: Int = shortcutDb.id
        if (shortcutId in newSelectedIds)
            newSelectedIds.remove(shortcutId)
        else
            newSelectedIds.add(shortcutId)
        state.update {
            it.copy(selectedIds = newSelectedIds)
        }
    }

    fun setSelectedIds(ids: Set<Int>) {
        state.update { it.copy(selectedIds = ids) }
    }

    fun getSelectedShortcutsDb(): List<ShortcutDb> =
        state.value.shortcutsDb.filter { it.id in state.value.selectedIds }
}
