package me.timeto.shared.ui.checklists

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.launchExIo
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.vm.__Vm

class ChecklistItemsFormVm(
    checklistDb: ChecklistDb,
) : __Vm<ChecklistItemsFormVm.State>() {

    data class State(
        val checklistDb: ChecklistDb,
        val checklistItemsDb: List<ChecklistItemDb>,
    ) {
        val checklistName: String = checklistDb.name
        val newItemText = "New Item"
    }

    override val state = MutableStateFlow(
        State(
            checklistDb = checklistDb,
            checklistItemsDb = Cache.checklistItemsDb
                .filter { it.list_id == checklistDb.id },
        )
    )

    init {
        val scopeVm = scopeVm()
        combine(
            ChecklistDb.selectAscFlow(),
            ChecklistItemDb.selectSortedFlow(),
        ) { allChecklists, allItems ->
            val newChecklistDb: ChecklistDb =
                allChecklists.firstOrNull { it.id == checklistDb.id } ?: checklistDb
            val newItemsDb: List<ChecklistItemDb> = allItems
                .filter { it.list_id == checklistDb.id }
            state.update {
                it.copy(
                    checklistDb = newChecklistDb,
                    checklistItemsDb = newItemsDb,
                )
            }
        }.launchIn(scopeVm)
    }

    ///

    fun isDoneAllowed(
        dialogsManager: DialogsManager,
    ): Boolean {
        if (state.value.checklistItemsDb.isEmpty()) {
            dialogsManager.alert("Please add at least one item")
            return false
        }
        return true
    }

    fun deleteItem(
        itemDb: ChecklistItemDb,
    ): Unit = launchExIo {
        itemDb.delete()
    }

    fun up(itemUi: ChecklistItemUi) {
        val itemsUi = state.value.checklistItemsUi
        if (itemsUi.first() == itemUi)
            return
        val idx = itemsUi.indexOf(itemUi)
        val prevIdx = idx - 1
        val prevItemUi = itemsUi[prevIdx]
        val newItemsUi = itemsUi.toMutableList()
        newItemsUi[idx] = prevItemUi
        newItemsUi[prevIdx] = itemUi
        launchExDefault {
            newItemsUi.forEachIndexed { idx, itemUi ->
                itemUi.checklistItemDb.updateSort(idx)
            }
        }
    }

    fun down(itemUi: ChecklistItemUi) {
        val itemsUi = state.value.checklistItemsUi
        if (itemsUi.last() == itemUi)
            return
        val idx = itemsUi.indexOf(itemUi)
        val nextIdx = idx + 1
        val nextItemUi = itemsUi[nextIdx]
        val newItemsUi = itemsUi.toMutableList()
        newItemsUi[idx] = nextItemUi
        newItemsUi[nextIdx] = itemUi
        launchExDefault {
            newItemsUi.forEachIndexed { idx, itemUi ->
                itemUi.checklistItemDb.updateSort(idx)
            }
        }
    }

    fun moveIos(from: Int, to: Int) {
        val oldList = state.value.checklistItemsDb
        val newList = oldList.toMutableList()
        val fromItem = oldList[from]
        newList.removeAt(from)
        newList.add(to, fromItem)
        launchExIo {
            ChecklistItemDb.updateSortMany(itemsDb = newList)
        }
    }
}
