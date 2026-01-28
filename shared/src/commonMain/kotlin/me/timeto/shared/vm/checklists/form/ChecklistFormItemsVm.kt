package me.timeto.shared.vm.checklists.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.launchExIo
import me.timeto.shared.DialogsManager
import me.timeto.shared.moveUiListAndroid
import me.timeto.shared.moveUiListIos
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.Vm

class ChecklistFormItemsVm(
    checklistDb: ChecklistDb,
) : Vm<ChecklistFormItemsVm.State>() {

    data class State(
        val checklistDb: ChecklistDb,
        val checklistItemsUi: List<ChecklistItemUi>,
    ) {
        val checklistName: String = checklistDb.name
        val newItemText = "New Item"
    }

    override val state = MutableStateFlow(
        State(
            checklistDb = checklistDb,
            checklistItemsUi = Cache.checklistItemsDb
                .filter { it.list_id == checklistDb.id }
                .map { ChecklistItemUi(it) },
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
                    checklistItemsUi = newItemsDb.map { ChecklistItemUi(it) },
                )
            }
        }.launchIn(scopeVm)
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

    fun deleteItemWithConfirmation(
        itemDb: ChecklistItemDb,
        dialogsManager: DialogsManager,
    ) {
        dialogsManager.confirmation(
            message = "Are you sure you want to delete \"${itemDb.text.textFeatures().textNoFeatures}\"?",
            buttonText = "Delete",
            onConfirm = {
                launchExIo {
                    itemDb.delete()
                }
            },
        )
    }

    //
    // Move Android

    fun moveAndroidLocal(fromIdx: Int, toIdx: Int) {
        state.value.checklistItemsUi.moveUiListAndroid(fromIdx, toIdx) { newItems ->
            state.update { it.copy(checklistItemsUi = newItems) }
        }
    }

    fun moveAndroidSync() {
        launchExIo {
            ChecklistItemDb.updateSortMany(state.value.checklistItemsUi.map { it.checklistItemDb })
        }
    }

    ///

    fun moveIos(fromIdx: Int, toIdx: Int) {
        state.value.checklistItemsUi.moveUiListIos(fromIdx, toIdx) {
            ChecklistItemDb.updateSortMany(itemsDb = it.map { it.checklistItemDb })
        }
    }

    ///

    data class ChecklistItemUi(
        val checklistItemDb: ChecklistItemDb,
    ) {
        val text: String =
            checklistItemDb.text.textFeatures().textNoFeatures
    }
}
