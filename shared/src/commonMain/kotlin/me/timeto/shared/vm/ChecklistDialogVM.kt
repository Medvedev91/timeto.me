package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.DI
import me.timeto.shared.db.ChecklistItemModel
import me.timeto.shared.db.ChecklistModel
import me.timeto.shared.launchExDefault
import me.timeto.shared.onEachExIn

// todo actions from view to vm
class ChecklistDialogVM(
    val checklist: ChecklistModel,
) : __VM<ChecklistDialogVM.State>() {

    data class State(
        val items: List<ChecklistItemModel>,
    )

    override val state = MutableStateFlow(
        State(items = DI.checklistItems.prepChecklistItems(checklist))
    )

    override fun onAppear() {
        val scope = scopeVM()
        ChecklistItemModel.getAscFlow().onEachExIn(scope) { items ->
            state.update { it.copy(items = items.prepChecklistItems(checklist)) }
        }
    }

    fun uncheck() {
        launchExDefault {
            ChecklistItemModel.toggleByList(checklist, false)
        }
    }
}

private fun List<ChecklistItemModel>.prepChecklistItems(
    checklist: ChecklistModel
): List<ChecklistItemModel> = this.filter { it.list_id == checklist.id }
