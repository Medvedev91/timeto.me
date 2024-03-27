package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.DI
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.launchExDefault
import me.timeto.shared.onEachExIn
import me.timeto.shared.vm.ui.ChecklistStateUI

class ChecklistVM(
    private val checklistDb: ChecklistDb,
) : __VM<ChecklistVM.State>() {

    data class State(
        val checklistUI: ChecklistUI,
    )

    override val state = MutableStateFlow(
        State(
            checklistUI = ChecklistUI.build(checklistDb, DI.checklistItems),
        )
    )

    override fun onAppear() {
        val scopeVm = scopeVM()
        ChecklistItemDb
            .getSortedFlow()
            .onEachExIn(scopeVm) { items ->
                state.update {
                    it.copy(checklistUI = ChecklistUI.build(checklistDb, items))
                }
            }
    }

    ///

    class ChecklistUI(
        val checklistDb: ChecklistDb,
        val items: List<ChecklistItemDb>,
    ) {

        companion object {

            fun build(
                checklistDb: ChecklistDb,
                allItemsDb: List<ChecklistItemDb>,
            ) = ChecklistUI(
                checklistDb = checklistDb,
                items = allItemsDb.filter { it.list_id == checklistDb.id },
            )
        }

        val stateUI = ChecklistStateUI.build(checklistDb, items)
        val itemsUI = items.map { ItemUI(it) }

        class ItemUI(
            val item: ChecklistItemDb,
        ) {
            fun toggle() {
                launchExDefault {
                    item.toggle()
                }
            }
        }
    }
}
