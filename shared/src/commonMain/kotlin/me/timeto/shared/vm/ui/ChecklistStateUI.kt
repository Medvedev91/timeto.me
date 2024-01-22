package me.timeto.shared.vm.ui

import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.launchExDefault

sealed class ChecklistStateUI(
    val actionDesc: String,
    val onClick: () -> Unit,
) {

    companion object {

        fun build(
            checklist: ChecklistDb,
            items: List<ChecklistItemDb>,
        ): ChecklistStateUI = when {
            items.all { it.isChecked } -> Completed(checklist)
            items.none { it.isChecked } -> Empty(checklist)
            else -> Partial(checklist)
        }
    }

    //////

    class Completed(checklist: ChecklistDb) : ChecklistStateUI("Uncheck All", {
        launchExDefault {
            ChecklistItemDb.toggleByList(checklist, false)
        }
    })

    class Empty(checklist: ChecklistDb) : ChecklistStateUI("Check All", {
        launchExDefault {
            ChecklistItemDb.toggleByList(checklist, true)
        }
    })

    class Partial(checklist: ChecklistDb) : ChecklistStateUI("Uncheck All", {
        launchExDefault {
            ChecklistItemDb.toggleByList(checklist, false)
        }
    })
}
