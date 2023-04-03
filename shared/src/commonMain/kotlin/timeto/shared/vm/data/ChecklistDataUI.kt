package timeto.shared.vm.data

import timeto.shared.db.ChecklistItemModel
import timeto.shared.db.ChecklistModel
import timeto.shared.defaultScope
import timeto.shared.launchEx
import timeto.shared.launchExDefault

class ChecklistDataUI(
    val checklist: ChecklistModel,
    val itemsUI: List<ItemUI>,
) {

    val completionState: CompletionState = when {
        itemsUI.all { it.item.isChecked } -> CompletionState.Completed(checklist)
        itemsUI.none { it.item.isChecked } -> CompletionState.Empty(checklist)
        else -> CompletionState.Partial(checklist)
    }

    ///

    companion object {

        fun build(
            checklist: ChecklistModel,
            items: List<ChecklistItemModel>,
        ) = ChecklistDataUI(
            checklist = checklist,
            itemsUI = items.map { ItemUI(it) },
        )
    }

    class ItemUI(
        val item: ChecklistItemModel,
    ) {
        fun toggle() {
            defaultScope().launchEx {
                item.toggle()
            }
        }
    }

    sealed class CompletionState(
        val actionDesc: String,
        val onClick: () -> Unit,
    ) {

        class Completed(checklist: ChecklistModel) : CompletionState("Uncheck All", {
            launchExDefault {
                ChecklistItemModel.toggleByList(checklist, false)
            }
        })

        class Empty(checklist: ChecklistModel) : CompletionState("Check All", {
            launchExDefault {
                ChecklistItemModel.toggleByList(checklist, true)
            }
        })

        class Partial(checklist: ChecklistModel) : CompletionState("Uncheck All", {
            launchExDefault {
                ChecklistItemModel.toggleByList(checklist, false)
            }
        })
    }
}
