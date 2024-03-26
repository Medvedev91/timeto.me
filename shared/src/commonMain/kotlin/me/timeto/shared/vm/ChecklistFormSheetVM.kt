package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.onEachExIn

class ChecklistFormSheetVM(
    checklistDb: ChecklistDb,
) : __VM<ChecklistFormSheetVM.State>() {

    data class State(
        val checklistDb: ChecklistDb,
    ) {
        val checklistName: String = checklistDb.name
    }

    override val state = MutableStateFlow(
        State(
            checklistDb = checklistDb,
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        ChecklistDb.getAscFlow().onEachExIn(scope) { allChecklists ->
            allChecklists
                .firstOrNull { it.id == state.value.checklistDb.id }
                ?.let { newChecklistDb ->
                    state.update { it.copy(checklistDb = newChecklistDb) }
                }
        }
    }
}
