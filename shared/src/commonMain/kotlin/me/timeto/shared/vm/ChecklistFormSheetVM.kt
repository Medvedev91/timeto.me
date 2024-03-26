package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.UIConfirmationData
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.launchExDefault
import me.timeto.shared.onEachExIn
import me.timeto.shared.showUiConfirmation

class ChecklistFormSheetVM(
    checklistDb: ChecklistDb,
) : __VM<ChecklistFormSheetVM.State>() {

    data class State(
        val checklistDb: ChecklistDb,
    ) {
        val checklistName: String = checklistDb.name
    }

    override val state = MutableStateFlow(
        State(
            checklistDb = checklistDb,
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        ChecklistDb.getAscFlow().onEachExIn(scope) { allChecklists ->
            allChecklists
                .firstOrNull { it.id == state.value.checklistDb.id }
                ?.let { newChecklistDb ->
                    state.update { it.copy(checklistDb = newChecklistDb) }
                }
        }
    }

    ///

    fun deleteChecklist(
        onDelete: () -> Unit,
    ) {
        val checklistDb = state.value.checklistDb
        showUiConfirmation(
            UIConfirmationData(
                text = "Are you sure you want to delete \"${checklistDb.name}\" checklist?",
                buttonText = "Delete",
                isRed = true,
                onConfirm = {
                    launchExDefault {
                        checklistDb.deleteWithDependencies()
                    }
                    onDelete()
                }
            )
        )
    }
}
