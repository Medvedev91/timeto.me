package timeto.shared.vm.ui

import timeto.shared.db.ChecklistItemModel
import timeto.shared.db.ChecklistModel
import timeto.shared.defaultScope
import timeto.shared.launchEx

class ChecklistUI(
    val checklist: ChecklistModel,
    val itemsUI: List<ItemUI>,
) {

    class ItemUI(
        val item: ChecklistItemModel,
    ) {
        fun toggle() {
            defaultScope().launchEx {
                item.toggle()
            }
        }
    }
}

fun ChecklistModel.toChecklistUI(
    items: List<ChecklistItemModel>,
) = ChecklistUI(
    checklist = this,
    itemsUI = items.map { ChecklistUI.ItemUI(it) },
)
