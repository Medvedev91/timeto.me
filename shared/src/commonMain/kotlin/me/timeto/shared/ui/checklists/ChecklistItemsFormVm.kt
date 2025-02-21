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
import me.timeto.shared.ui.moveAndroid
import me.timeto.shared.ui.moveIos
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

    fun moveIos(fromIdx: Int, toIdx: Int) {
        state.value.checklistItemsDb.moveIos(fromIdx, toIdx) {
            ChecklistItemDb.updateSortMany(itemsDb = it)
        }
    }

    fun moveAndroidLocal(fromIdx: Int, toIdx: Int) {
        state.value.checklistItemsDb.moveAndroid(fromIdx, toIdx) { newItems ->
            state.update { it.copy(checklistItemsDb = newItems) }
        }
    }

    fun moveAndroidSync() {
        launchExIo {
            ChecklistItemDb.updateSortMany(state.value.checklistItemsDb)
        }
    }
}
