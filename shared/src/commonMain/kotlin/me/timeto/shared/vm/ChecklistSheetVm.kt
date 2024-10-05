package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.Cache
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.onEachExIn

class ChecklistSheetVm(
    private val checklistDb: ChecklistDb,
) : __Vm<ChecklistSheetVm.State>() {

    data class State(
        val checklistDb: ChecklistDb,
        val items: List<ChecklistItemDb>,
    )

    override val state = MutableStateFlow(
        State(
            checklistDb = checklistDb,
            items = Cache.checklistItemsDb.prepChecklistItems(checklistDb),
        )
    )

    override fun onAppear() {
        val scope = scopeVm()
        ChecklistItemDb.getSortedFlow().onEachExIn(scope) { items ->
            state.update { it.copy(items = items.prepChecklistItems(checklistDb)) }
        }
    }
}

private fun List<ChecklistItemDb>.prepChecklistItems(
    checklist: ChecklistDb
): List<ChecklistItemDb> = this.filter { it.list_id == checklist.id }
