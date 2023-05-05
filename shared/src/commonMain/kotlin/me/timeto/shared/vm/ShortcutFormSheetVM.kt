package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.UIException
import timeto.shared.db.ShortcutModel
import timeto.shared.launchEx
import timeto.shared.showUiAlert

class ShortcutFormSheetVM(
    val shortcut: ShortcutModel?
) : __VM<ShortcutFormSheetVM.State>() {

    data class State(
        val headerTitle: String,
        val headerDoneText: String,
        val inputNameValue: String,
        val inputUriValue: String,
    ) {
        val isHeaderDoneEnabled = (inputNameValue.isNotBlank() && inputUriValue.isNotBlank())
        val inputNameHeader = "SHORTCUT NAME"
        val inputNamePlaceholder = "Name"
        val inputUriHeader = "SHORTCUT LINK"
        val inputUriPlaceholder = "Link"
    }

    override val state: MutableStateFlow<State>

    init {
        state = MutableStateFlow(
            State(
                headerTitle = if (shortcut != null) "Edit Shortcut" else "New Shortcut",
                headerDoneText = if (shortcut != null) "Done" else "Create",
                inputNameValue = shortcut?.name ?: "",
                inputUriValue = shortcut?.uri ?: "",
            )
        )
    }

    fun setInputNameValue(text: String) = state.update {
        it.copy(inputNameValue = text)
    }

    fun setInputUriValue(text: String) = state.update {
        it.copy(inputUriValue = text)
    }

    fun save(
        onSuccess: () -> Unit
    ): Unit = scopeVM().launchEx {
        try {
            if (shortcut != null)
                shortcut.upWithValidation(
                    name = state.value.inputNameValue,
                    uri = state.value.inputUriValue,
                )
            else
                ShortcutModel.addWithValidation(
                    name = state.value.inputNameValue,
                    uri = state.value.inputUriValue,
                )
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
