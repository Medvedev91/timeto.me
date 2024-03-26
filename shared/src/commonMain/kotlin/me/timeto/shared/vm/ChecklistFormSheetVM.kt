package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ChecklistDb

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
    }
}
