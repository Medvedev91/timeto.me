package timeto.shared.vm.ui

import timeto.shared.db.ChecklistItemModel
import timeto.shared.db.ChecklistModel

class ChecklistUI(
    val checklist: ChecklistModel,
    val itemsUI: List<ItemUI>,
) {

    class ItemUI(
        val item: ChecklistItemModel,
    )
}

fun ChecklistModel.toChecklistUI(
    items: List<ChecklistItemModel>,
) = ChecklistUI(
    checklist = this,
    itemsUI = items.map { ChecklistUI.ItemUI(it) },
)
