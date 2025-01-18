package me.timeto.shared.vm.Checklist

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.vm.__Vm

class ChecklistFormVm(
    checklistDb: ChecklistDb,
) : __Vm<ChecklistFormVm.State>() {

    data class State(
        val checklistDb: ChecklistDb,
        val checklistItemsDb: List<ChecklistItemDb>,
    ) {

        val newItemButtonText = "New Item"

        val checklistName: String = checklistDb.name
        val checklistItemsUi: List<ChecklistItemUi> = checklistItemsDb
            .mapIndexed { idx, checklistItemDb ->
                ChecklistItemUi(
                    checklistItemDb = checklistItemDb,
                    isFirst = (idx == 0),
                )
            }
    }

    override val state = MutableStateFlow(
        State(
            checklistDb = checklistDb,
            checklistItemsDb = Cache.checklistItemsDb.filter { it.list_id == checklistDb.id },
        )
    )

    init {
        val scope = scopeVm()
        ChecklistDb.selectAscFlow().onEachExIn(scope) { allChecklists ->
            allChecklists
                .firstOrNull { it.id == state.value.checklistDb.id }
                ?.let { newChecklistDb ->
                    state.update { it.copy(checklistDb = newChecklistDb) }
                }
        }
        ChecklistItemDb.selectSortedFlow().onEachExIn(scope) { allChecklistItems ->
            val newChecklistItemsDb = allChecklistItems
                .filter { it.list_id == state.value.checklistDb.id }
            state.update { it.copy(checklistItemsDb = newChecklistItemsDb) }
        }
    }

    ///

    fun isDoneAllowed(
        dialogsManager: DialogsManager,
    ): Boolean {
        if (state.value.checklistItemsUi.isEmpty()) {
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

    fun moveByIdxIos(fromIdx: Int, toIdx: Int) {
        val list = state.value.checklistItemsDb
        val newList = list.toMutableList()
        val fromItem = list[fromIdx]
        newList.removeAt(fromIdx)
        newList.add(toIdx, fromItem)
        // Do together
        state.update { it.copy(checklistItemsDb = newList) }
        launchExIo {
            ChecklistItemDb.updateSortMany(itemsDb = newList)
        }
    }

    ///

    data class ChecklistItemUi(
        val checklistItemDb: ChecklistItemDb,
        val isFirst: Boolean,
    )
}
