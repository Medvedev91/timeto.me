package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.misc.DialogsManager

class ChecklistFormSheetVm(
    checklistDb: ChecklistDb,
) : __Vm<ChecklistFormSheetVm.State>() {

    data class State(
        val checklistDb: ChecklistDb,
        val checklistItemsDb: List<ChecklistItemDb>,
    ) {

        val newItemButton = "+ new item"

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

    override fun onAppear() {
        val scope = scopeVm()
        ChecklistDb.selectAscFlow().onEachExIn(scope) { allChecklists ->
            allChecklists
                .firstOrNull { it.id == state.value.checklistDb.id }
                ?.let { newChecklistDb ->
                    state.update { it.copy(checklistDb = newChecklistDb) }
                }
        }
        ChecklistItemDb.getSortedFlow().onEachExIn(scope) { allChecklistItems ->
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
            dialogsManager.alert("Please add at least one item.")
            return false
        }
        return true
    }

    fun deleteItem(itemDb: ChecklistItemDb) {
        showUiConfirmation(
            UIConfirmationData(
                text = "Are you sure you want to delete \"${itemDb.text}\"?",
                buttonText = "Delete",
                isRed = true,
                onConfirm = {
                    launchExDefault {
                        itemDb.delete()
                    }
                }
            )
        )
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
                itemUi.checklistItemDb.upSort(idx)
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
                itemUi.checklistItemDb.upSort(idx)
            }
        }
    }

    ///

    data class ChecklistItemUi(
        val checklistItemDb: ChecklistItemDb,
        val isFirst: Boolean,
    )
}
