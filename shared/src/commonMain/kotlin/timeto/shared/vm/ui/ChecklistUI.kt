package timeto.shared.vm.ui

import timeto.shared.db.ChecklistItemModel
import timeto.shared.db.ChecklistModel
import timeto.shared.defaultScope
import timeto.shared.launchEx
import timeto.shared.launchExDefault

class ChecklistUI(
    val checklist: ChecklistModel,
    val itemsUI: List<ItemUI>,
) {

    val completionState: CompletionState = when {
        itemsUI.all { it.item.isChecked } -> CompletionState.Completed(checklist)
        itemsUI.none { it.item.isChecked } -> CompletionState.Empty(checklist)
        else -> CompletionState.Partial(checklist)
    }

    ///

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

fun ChecklistModel.toChecklistUI(
    items: List<ChecklistItemModel>,
) = ChecklistUI(
    checklist = this,
    itemsUI = items.map { ChecklistUI.ItemUI(it) },
)
