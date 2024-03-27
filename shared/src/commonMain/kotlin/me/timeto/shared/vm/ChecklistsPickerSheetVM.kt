package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.DI
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.onEachExIn

class ChecklistsPickerSheetVM(
    private val selectedChecklists: List<ChecklistDb>,
) : __VM<ChecklistsPickerSheetVM.State>() {

    data class State(
        val checklistsUI: List<ChecklistUI>,
    ) {
        val headerTitle = "Checklists"
        val doneTitle = "Done"
        val newChecklistButton = "+ new checklist"
    }

    data class ChecklistUI(
        val checklist: ChecklistDb,
        val isSelected: Boolean,
    ) {
        val text = checklist.name
    }

    override val state = MutableStateFlow(
        State(
            checklistsUI = prepChecklistsUi(DI.checklists, selectedChecklists),
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        ChecklistDb.getAscFlow().onEachExIn(scope) { newChecklistsDb ->
            state.update {
                it.copy(checklistsUI = prepChecklistsUi(newChecklistsDb, selectedChecklists))
            }
        }
    }

    ///

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

private fun prepChecklistsUi(
    allChecklistsDb: List<ChecklistDb>,
    selectedChecklists: List<ChecklistDb>,
): List<ChecklistsPickerSheetVM.ChecklistUI> {
    val selectedChecklistIds = selectedChecklists.map { it.id }
    return allChecklistsDb.map {
        ChecklistsPickerSheetVM.ChecklistUI(it, it.id in selectedChecklistIds)
    }
}
