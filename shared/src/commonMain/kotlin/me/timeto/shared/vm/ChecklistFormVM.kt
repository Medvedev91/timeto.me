package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.UIException
import timeto.shared.db.ChecklistModel
import timeto.shared.launchEx
import timeto.shared.showUiAlert

class ChecklistFormVM(
    val checklist: ChecklistModel?,
) : __VM<ChecklistFormVM.State>() {

    data class State(
        val inputNameValue: String,
    ) {
        val isSaveEnabled = inputNameValue.isNotBlank()
    }

    override val state: MutableStateFlow<State>

    init {
        state = MutableStateFlow(
            State(
                inputNameValue = checklist?.name ?: ""
            )
        )
    }

    fun setInputName(name: String) {
        state.update { it.copy(inputNameValue = name) }
    }

    fun save(
        onSuccess: () -> Unit,
    ) = scopeVM().launchEx {
        try {
            if (checklist != null)
                checklist.upNameWithValidation(state.value.inputNameValue)
            else
                ChecklistModel.addWithValidation(state.value.inputNameValue)
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
