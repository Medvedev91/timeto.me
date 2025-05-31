package me.timeto.shared.ui.checklists.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.launchExIo
import me.timeto.shared.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.ui.__Vm

class ChecklistFormVm(
    checklistDb: ChecklistDb?,
) : __Vm<ChecklistFormVm.State>() {

    data class State(
        val checklistDb: ChecklistDb?,
        val name: String,
    ) {

        val title: String = if (checklistDb != null) "Edit Checklist" else "New Checklist"
        val doneText: String = if (checklistDb != null) "Save" else "Next"
        val isSaveEnabled: Boolean = name.isNotBlank()
        val deleteText = "Delete Checklist"

        val namePlaceholder = "Name"
    }

    override val state = MutableStateFlow(
        State(
            checklistDb = checklistDb,
            name = checklistDb?.name ?: "",
        )
    )

    fun setName(name: String) {
        state.update { it.copy(name = name) }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: (ChecklistDb) -> Unit,
    ): Unit = launchExIo {
        try {
            val oldChecklistDb: ChecklistDb? = state.value.checklistDb
            val name: String = state.value.name
            val newChecklistDb: ChecklistDb = if (oldChecklistDb != null)
                oldChecklistDb.updateWithValidation(name = name)
            else
                ChecklistDb.insertWithValidation(name = name)
            onUi { onSuccess(newChecklistDb) }
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }

    fun delete(
        checklistDb: ChecklistDb,
        dialogsManager: DialogsManager,
        onDelete: () -> Unit,
    ) {
        dialogsManager.confirmation(
            message = "Are you sure you want to delete \"${checklistDb.name}\" checklist?",
            buttonText = "Delete",
            onConfirm = {
                launchExIo {
                    checklistDb.deleteWithDependencies()
                    onUi { onDelete() }
                }
            },
        )
    }
}
