package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.UIException
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.launchEx
import me.timeto.shared.showUiAlert

class ChecklistItemFormVm(
    val checklistDb: ChecklistDb,
    val checklistItemDb: ChecklistItemDb?,
) : __Vm<ChecklistItemFormVm.State>() {

    data class State(
        val inputNameValue: String,
    ) {
        val isSaveEnabled = inputNameValue.isNotBlank()
    }

    override val state: MutableStateFlow<State>

    init {
        state = MutableStateFlow(
            State(
                inputNameValue = checklistItemDb?.text ?: ""
            )
        )
    }

    fun setInputName(name: String) {
        state.update { it.copy(inputNameValue = name) }
    }

    fun save(
        onSuccess: () -> Unit
    ) = scopeVm().launchEx {
        try {
            if (checklistItemDb != null)
                checklistItemDb.upTextWithValidation(state.value.inputNameValue)
            else
                ChecklistItemDb.addWithValidation(state.value.inputNameValue, checklistDb)
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
