package me.timeto.shared.ui.checklists

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.launchExIo
import me.timeto.shared.onEachExIn
import me.timeto.shared.DialogsManager
import me.timeto.shared.moveUiListIos
import me.timeto.shared.ui.__Vm

class ChecklistVm(
    checklistDb: ChecklistDb,
) : __Vm<ChecklistVm.State>() {

    data class State(
        val checklistDb: ChecklistDb,
        val itemsDb: List<ChecklistItemDb>,
    ) {

        val stateUi: ChecklistStateUi =
            ChecklistStateUi.build(checklistDb, itemsDb)

        val itemsUi: List<ItemUi> =
            itemsDb.map { ItemUi(itemDb = it) }
    }

    override val state = MutableStateFlow(
        State(
            checklistDb = checklistDb,
            itemsDb = checklistDb.getItemsCached(),
        )
    )

    init {
        val scopeVm = scopeVm()
        ChecklistItemDb.selectSortedFlow().onEachExIn(scopeVm) { itemsDb ->
            state.update {
                it.copy(itemsDb = itemsDb.filter { it.list_id == checklistDb.id })
            }
        }
    }

    fun deleteItem(
        itemDb: ChecklistItemDb,
        dialogsManager: DialogsManager,
    ) {
        dialogsManager.confirmation(
            message = "Are you sure you want to delete \"${itemDb.text}\"?",
            buttonText = "Delete",
            onConfirm = {
                launchExIo {
                    itemDb.delete()
                }
            },
        )
    }

    fun moveIos(fromIdx: Int, toIdx: Int) {
        state.value.itemsDb.moveUiListIos(fromIdx, toIdx) {
            ChecklistItemDb.updateSortMany(itemsDb = it)
        }
    }

    ///

    class ItemUi(
        val itemDb: ChecklistItemDb,
    ) {
        fun toggle() {
            launchExIo {
                itemDb.toggle()
            }
        }
    }
}
