package me.timeto.shared.vm.Checklist

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.launchExDefault
import me.timeto.shared.onEachExIn
import me.timeto.shared.models.ChecklistStateUi
import me.timeto.shared.vm.__Vm

class ChecklistVm(
    private val checklistDb: ChecklistDb,
) : __Vm<ChecklistVm.State>() {

    data class State(
        val checklistUI: ChecklistUI,
    )

    override val state = MutableStateFlow(
        State(
            checklistUI = ChecklistUI.build(checklistDb, checklistDb.getItemsCached()),
        )
    )

    init {
        val scopeVm = scopeVm()
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

        val stateUI = ChecklistStateUi.build(checklistDb, items)
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
