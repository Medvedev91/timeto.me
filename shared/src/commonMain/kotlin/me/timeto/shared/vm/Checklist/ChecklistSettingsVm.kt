package me.timeto.shared.vm.Checklist

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.launchExIo
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.ui.UiException
import me.timeto.shared.vm.__Vm

class ChecklistSettingsVm(
    checklistDb: ChecklistDb?,
) : __Vm<ChecklistSettingsVm.State>() {

    data class State(
        val checklistDb: ChecklistDb?,
        val name: String,
    ) {

        val title: String = if (checklistDb != null) "Edit Checklist" else "New Checklist"
        val saveButtonText: String = if (checklistDb != null) "Save" else "Next"
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
