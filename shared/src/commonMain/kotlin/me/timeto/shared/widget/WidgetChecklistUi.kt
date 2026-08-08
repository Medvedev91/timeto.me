package me.timeto.shared.widget

import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.launchExIo
import me.timeto.shared.textFeatures

data class WidgetChecklistUi(
    val checklistDb: ChecklistDb,
    val itemsUi: List<ItemUi>,
) {

    class ItemUi(
        val itemDb: ChecklistItemDb,
    ) {

        val textFeatures: TextFeatures =
            itemDb.text.textFeatures()

        val text: String =
            textFeatures.textNoFeatures

        fun toggle() {
            launchExIo {
                itemDb.toggle()
            }
        }
    }
}
