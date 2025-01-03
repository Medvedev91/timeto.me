package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.UIException
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.launchEx
import me.timeto.shared.showUiAlert

class ChecklistNameDialogVm(
    checklistDb: ChecklistDb?,
) : __Vm<ChecklistNameDialogVm.State>() {

    data class State(
        val checklistDb: ChecklistDb?,
        val input: String,
    ) {
        val isSaveEnabled: Boolean = input.isNotBlank()
        val title: String = checklistDb?.name ?: "New Checklist"
        val placeholder = "Name"
    }

    override val state = MutableStateFlow(
        State(
            checklistDb = checklistDb,
            input = checklistDb?.name ?: ""
        )
    )

    fun setInput(value: String) {
        state.update { it.copy(input = value) }
    }

    fun save(
        onSuccess: (ChecklistDb) -> Unit,
    ) {
        scopeVm().launchEx {
            try {
                val oldChecklistDb: ChecklistDb? = state.value.checklistDb
                val newChecklistDb: ChecklistDb = if (oldChecklistDb != null)
                    oldChecklistDb.upNameWithValidation(state.value.input)
                else
                    ChecklistDb.addWithValidation(state.value.input)
                onSuccess(newChecklistDb)
            } catch (e: UIException) {
                showUiAlert(e.uiMessage)
            }
        }
    }
}
