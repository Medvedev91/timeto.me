package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.launchEx
import me.timeto.shared.misc.DialogsManager
import me.timeto.shared.ui.UiException

class ChecklistItemFormVm(
    checklistDb: ChecklistDb,
    checklistItemDb: ChecklistItemDb?,
) : __Vm<ChecklistItemFormVm.State>() {

    data class State(
        val checklistDb: ChecklistDb,
        val checklistItemDb: ChecklistItemDb?,
        val text: String,
    ) {

        val title = "New Item"
        val saveButtonText = "Save"
        val isSaveEnabled: Boolean = text.isNotBlank()
    }

    override val state = MutableStateFlow(
        State(
            checklistDb = checklistDb,
            checklistItemDb = checklistItemDb,
            text = checklistItemDb?.text ?: ""
        )
    )

    fun setText(text: String) {
        state.update { it.copy(text = text) }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit
    ): Unit = scopeVm().launchEx {
        try {
            val text: String = state.value.text
            val checklistDb: ChecklistDb = state.value.checklistDb
            val oldItemDb: ChecklistItemDb? = state.value.checklistItemDb
            if (oldItemDb != null)
                oldItemDb.updateTextWithValidation(text)
            else
                ChecklistItemDb.insertWithValidation(text, checklistDb)
            onUi { onSuccess() }
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }
}
