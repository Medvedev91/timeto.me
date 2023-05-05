package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.DI
import me.timeto.shared.db.ChecklistModel

class ChecklistsPickerSheetVM(
    selectedChecklists: List<ChecklistModel>,
) : __VM<ChecklistsPickerSheetVM.State>() {

    data class State(
        val checklistsUI: List<ChecklistUI>,
    ) {
        val headerTitle = "Checklists"
        val doneTitle = "Done"
    }

    data class ChecklistUI(
        val checklist: ChecklistModel,
        val isSelected: Boolean,
    ) {
        val text = checklist.name
    }

    override val state = MutableStateFlow(
        State(
            checklistsUI = DI.checklists.map {
                ChecklistUI(it, it.id in selectedChecklists.map { it.id })
            },
        )
    )

    fun toggleChecklist(checklistUI: ChecklistUI) {
        val checklistsUI = state.value.checklistsUI.toMutableList()
        val idx = checklistsUI.indexOf(checklistUI) // todo report if -1
        checklistsUI[idx] = checklistUI.copy(isSelected = !checklistUI.isSelected)
        state.update { it.copy(checklistsUI = checklistsUI) }
    }

    fun getSelectedChecklists() = state.value.checklistsUI
        .filter { it.isSelected }
        .map { it.checklist }
}
