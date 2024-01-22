package me.timeto.shared.vm.ui

import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.db.ChecklistModel
import me.timeto.shared.launchExDefault

sealed class ChecklistStateUI(
    val actionDesc: String,
    val onClick: () -> Unit,
) {

    companion object {

        fun build(
            checklist: ChecklistModel,
            items: List<ChecklistItemDb>,
        ): ChecklistStateUI = when {
            items.all { it.isChecked } -> Completed(checklist)
            items.none { it.isChecked } -> Empty(checklist)
            else -> Partial(checklist)
        }
    }

    //////

    class Completed(checklist: ChecklistModel) : ChecklistStateUI("Uncheck All", {
        launchExDefault {
            ChecklistItemDb.toggleByList(checklist, false)
        }
    })

    class Empty(checklist: ChecklistModel) : ChecklistStateUI("Check All", {
        launchExDefault {
            ChecklistItemDb.toggleByList(checklist, true)
        }
    })

    class Partial(checklist: ChecklistModel) : ChecklistStateUI("Uncheck All", {
        launchExDefault {
            ChecklistItemDb.toggleByList(checklist, false)
        }
    })
}
