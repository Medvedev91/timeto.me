package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.UIException
import timeto.shared.db.ChecklistItemModel
import timeto.shared.db.ChecklistModel
import timeto.shared.launchEx
import timeto.shared.showUiAlert

class ChecklistItemFormVM(
    val checklist: ChecklistModel,
    val checklistItem: ChecklistItemModel?,
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
                ChecklistItemModel.addWithValidation(state.value.inputNameValue, checklist)
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
