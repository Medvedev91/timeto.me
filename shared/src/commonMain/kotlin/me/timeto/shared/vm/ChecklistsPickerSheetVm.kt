package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.Cache
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.onEachExIn

class ChecklistsPickerSheetVm(
    selectedChecklists: List<ChecklistDb>,
) : __Vm<ChecklistsPickerSheetVm.State>() {

    data class State(
        val checklistsDb: List<ChecklistDb>,
        val selectedIds: Set<Int>,
    ) {
        val headerTitle = "Checklists"
        val doneTitle = "Done"
        val newChecklistButton = "+ new checklist"

        val checklistsUI: List<ChecklistUI> = checklistsDb.map { checklistDb ->
            ChecklistUI(
                checklist = checklistDb,
                isSelected = checklistDb.id in selectedIds,
            )
        }
    }

    data class ChecklistUI(
        val checklist: ChecklistDb,
        val isSelected: Boolean,
    ) {
        val text = checklist.name
    }

    override val state = MutableStateFlow(
        State(
            checklistsDb = Cache.checklistsDb,
            selectedIds = selectedChecklists.map { it.id }.toSet(),
        )
    )

    override fun onAppear() {
        val scope = scopeVm()
        ChecklistDb.selectAscFlow().onEachExIn(scope) { newChecklistsDb ->
            state.update {
                it.copy(checklistsDb = newChecklistsDb)
            }
        }
    }

    ///

    fun selectById(id: Int) {
        val newSelectedIds = state.value.selectedIds.toMutableSet()
        newSelectedIds.add(id)
        state.update {
            it.copy(selectedIds = newSelectedIds)
        }
    }

    fun toggleChecklist(checklistUI: ChecklistUI) {
        val newSelectedIds = state.value.selectedIds.toMutableSet()
        val checklistId = checklistUI.checklist.id
        if (checklistUI.isSelected)
            newSelectedIds.remove(checklistId)
        else
            newSelectedIds.add(checklistId)
        state.update {
            it.copy(selectedIds = newSelectedIds)
        }
    }

    fun getSelectedChecklists() = state.value.checklistsUI
        .filter { it.isSelected }
        .map { it.checklist }
}
