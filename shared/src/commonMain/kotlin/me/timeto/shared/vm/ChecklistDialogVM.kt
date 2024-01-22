package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.DI
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.launchExDefault
import me.timeto.shared.onEachExIn

// todo actions from view to vm
class ChecklistDialogVM(
    val checklist: ChecklistDb,
) : __VM<ChecklistDialogVM.State>() {

    data class State(
        val items: List<ChecklistItemDb>,
    )

    override val state = MutableStateFlow(
        State(items = DI.checklistItems.prepChecklistItems(checklist))
    )

    override fun onAppear() {
        val scope = scopeVM()
        ChecklistItemDb.getAscFlow().onEachExIn(scope) { items ->
            state.update { it.copy(items = items.prepChecklistItems(checklist)) }
        }
    }

    fun uncheck() {
        launchExDefault {
            ChecklistItemDb.toggleByList(checklist, false)
        }
    }
}

private fun List<ChecklistItemDb>.prepChecklistItems(
    checklist: ChecklistDb
): List<ChecklistItemDb> = this.filter { it.list_id == checklist.id }
