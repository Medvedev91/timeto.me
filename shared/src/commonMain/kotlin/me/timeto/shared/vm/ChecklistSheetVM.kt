package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.DI
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.onEachExIn

class ChecklistSheetVM(
    private val checklistDb: ChecklistDb,
) : __VM<ChecklistSheetVM.State>() {

    data class State(
        val checklistDb: ChecklistDb,
        val items: List<ChecklistItemDb>,
    )

    override val state = MutableStateFlow(
        State(
            checklistDb = checklistDb,
            items = DI.checklistItems.prepChecklistItems(checklistDb),
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        ChecklistItemDb.getSortedFlow().onEachExIn(scope) { items ->
            state.update { it.copy(items = items.prepChecklistItems(checklistDb)) }
        }
    }
}

private fun List<ChecklistItemDb>.prepChecklistItems(
    checklist: ChecklistDb
): List<ChecklistItemDb> = this.filter { it.list_id == checklist.id }
