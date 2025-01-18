package me.timeto.shared.ui.checklists

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.onEachExIn
import me.timeto.shared.vm.__Vm

class ChecklistScreenVm(
    checklistDb: ChecklistDb,
) : __Vm<ChecklistScreenVm.State>() {

    data class State(
        val checklistDb: ChecklistDb,
    )

    override val state = MutableStateFlow(
        State(
            checklistDb = checklistDb,
        )
    )

    init {
        val scopeVm = scopeVm()
        ChecklistDb.selectAscFlow().onEachExIn(scopeVm) { checklistsDb ->
            val newChecklistDb: ChecklistDb? =
                checklistsDb.firstOrNull { it.id == checklistDb.id }
            if (newChecklistDb != null)
                state.update { it.copy(checklistDb = newChecklistDb) }
        }
    }
}
