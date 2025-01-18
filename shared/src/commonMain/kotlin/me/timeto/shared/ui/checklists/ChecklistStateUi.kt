package me.timeto.shared.ui.checklists

import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.launchExIo

sealed class ChecklistStateUi(
    val actionDesc: String,
    val onClick: () -> Unit,
) {

    companion object {

        fun build(
            checklist: ChecklistDb,
            items: List<ChecklistItemDb>,
        ): ChecklistStateUi = when {
            items.all { it.isChecked } -> Completed(checklist)
            items.none { it.isChecked } -> Empty(checklist)
            else -> Partial(checklist)
        }
    }

    ///

    class Completed(checklist: ChecklistDb) : ChecklistStateUi("Uncheck All", {
        launchExIo {
            ChecklistItemDb.toggleByList(checklist, false)
        }
    })

    class Empty(checklist: ChecklistDb) : ChecklistStateUi("Check All", {
        launchExIo {
            ChecklistItemDb.toggleByList(checklist, true)
        }
    })

    class Partial(checklist: ChecklistDb) : ChecklistStateUi("Uncheck All", {
        launchExIo {
            ChecklistItemDb.toggleByList(checklist, false)
        }
    })
}
