package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ChecklistDb

class ChecklistFormSheetVM(
    checklistDb: ChecklistDb,
) : __VM<ChecklistFormSheetVM.State>() {

    data class State(
        val checklistName: String,
    )

    override val state = MutableStateFlow(
        State(
            checklistName = checklistDb.name,
        )
    )

    override fun onAppear() {
    }
}
