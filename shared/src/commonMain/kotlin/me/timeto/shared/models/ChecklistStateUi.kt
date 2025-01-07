package me.timeto.shared.models

import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.launchExDefault

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
        launchExDefault {
            ChecklistItemDb.toggleByList(checklist, false)
        }
    })

    class Empty(checklist: ChecklistDb) : ChecklistStateUi("Check All", {
        launchExDefault {
            ChecklistItemDb.toggleByList(checklist, true)
        }
    })

    class Partial(checklist: ChecklistDb) : ChecklistStateUi("Uncheck All", {
        launchExDefault {
            ChecklistItemDb.toggleByList(checklist, false)
        }
    })
}
