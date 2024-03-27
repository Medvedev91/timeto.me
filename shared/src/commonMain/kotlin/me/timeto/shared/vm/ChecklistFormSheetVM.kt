package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ChecklistItemDb

class ChecklistFormSheetVM(
    checklistDb: ChecklistDb,
) : __VM<ChecklistFormSheetVM.State>() {

    data class State(
        val checklistDb: ChecklistDb,
        val checklistItemsDb: List<ChecklistItemDb>,
    ) {
        val checklistName: String = checklistDb.name
        val checklistItemsUi: List<ChecklistItemUi> = checklistItemsDb
            .mapIndexed { idx, checklistItemDb ->
                ChecklistItemUi(
                    checklistItemDb = checklistItemDb,
                    isFirst = (idx == 0),
                )
            }
    }

    override val state = MutableStateFlow(
        State(
            checklistDb = checklistDb,
            checklistItemsDb = DI.checklistItems.filter { it.list_id == checklistDb.id },
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
        ChecklistItemDb.getSortedFlow().onEachExIn(scope) { allChecklistItems ->
            val newChecklistItemsDb = allChecklistItems
                .filter { it.list_id == state.value.checklistDb.id }
            state.update { it.copy(checklistItemsDb = newChecklistItemsDb) }
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

    fun deleteItem(itemDb: ChecklistItemDb) {
        showUiConfirmation(
            UIConfirmationData(
                text = "Are you sure you want to delete \"${itemDb.text}\"?",
                buttonText = "Delete",
                isRed = true,
                onConfirm = {
                    launchExDefault {
                        itemDb.delete()
                    }
                }
            )
        )
    }

    ///

    data class ChecklistItemUi(
        val checklistItemDb: ChecklistItemDb,
        val isFirst: Boolean,
    )
}
