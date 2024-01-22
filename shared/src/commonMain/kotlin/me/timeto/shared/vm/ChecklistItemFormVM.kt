package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.UIException
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.db.ChecklistModel
import me.timeto.shared.launchEx
import me.timeto.shared.showUiAlert

class ChecklistItemFormVM(
    val checklist: ChecklistModel,
    val checklistItem: ChecklistItemDb?,
) : __VM<ChecklistItemFormVM.State>() {

    data class State(
        val inputNameValue: String,
    ) {
        val isSaveEnabled = inputNameValue.isNotBlank()
    }

    override val state: MutableStateFlow<State>

    init {
        state = MutableStateFlow(
            State(
                inputNameValue = checklistItem?.text ?: ""
            )
        )
    }

    fun setInputName(name: String) {
        state.update { it.copy(inputNameValue = name) }
    }

    fun save(
        onSuccess: () -> Unit
    ) = scopeVM().launchEx {
        try {
            if (checklistItem != null)
                checklistItem.upTextWithValidation(state.value.inputNameValue)
            else
                ChecklistItemDb.addWithValidation(state.value.inputNameValue, checklist)
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
