package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.DI
import timeto.shared.db.ChecklistItemModel
import timeto.shared.db.ChecklistModel
import timeto.shared.onEachExIn

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
}

private fun List<ChecklistItemModel>.prepChecklistItems(
    checklist: ChecklistModel
): List<ChecklistItemModel> = this.filter { it.list_id == checklist.id }
