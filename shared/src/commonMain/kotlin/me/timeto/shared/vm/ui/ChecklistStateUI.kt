package timeto.shared.vm.ui

import timeto.shared.db.ChecklistItemModel
import timeto.shared.db.ChecklistModel
import timeto.shared.launchExDefault

sealed class ChecklistStateUI(
    val actionDesc: String,
    val onClick: () -> Unit,
) {

    companion object {

        fun build(
            checklist: ChecklistModel,
            items: List<ChecklistItemModel>,
        ): ChecklistStateUI = when {
            items.all { it.isChecked } -> Completed(checklist)
            items.none { it.isChecked } -> Empty(checklist)
            else -> Partial(checklist)
        }
    }

    //////

    class Completed(checklist: ChecklistModel) : ChecklistStateUI("Uncheck All", {
        launchExDefault {
            ChecklistItemModel.toggleByList(checklist, false)
        }
    })

    class Empty(checklist: ChecklistModel) : ChecklistStateUI("Check All", {
        launchExDefault {
            ChecklistItemModel.toggleByList(checklist, true)
        }
    })

    class Partial(checklist: ChecklistModel) : ChecklistStateUI("Uncheck All", {
        launchExDefault {
            ChecklistItemModel.toggleByList(checklist, false)
        }
    })
}
