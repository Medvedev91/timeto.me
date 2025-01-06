package me.timeto.shared.vm.ChecklistForm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.launchEx
import me.timeto.shared.misc.DialogsManager
import me.timeto.shared.misc.UiException
import me.timeto.shared.vm.__Vm

class ChecklistFormSettingsVm(
    checklistDb: ChecklistDb?,
) : __Vm<ChecklistFormSettingsVm.State>() {

    data class State(
        val id: Int?,
        val name: String,
    ) {

        val title: String = if (id != null) "Edit Checklist" else "New Checklist"
        val saveButtonText: String = if (id != null) "Save" else "Next"
        val isSaveEnabled: Boolean = name.isNotBlank()

        val namePlaceholder = "Name"
    }

    override val state = MutableStateFlow(
        State(
            id = checklistDb?.id,
            name = checklistDb?.name ?: "",
        )
    )

    fun setName(name: String) {
        state.update { it.copy(name = name) }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: (ChecklistDb) -> Unit,
    ) {
        scopeVm().launchEx {
            try {
                val oldId: Int? = state.value.id
                val name: String = state.value.name
                val newChecklistDb: ChecklistDb = if (oldId != null)
                    TODO()
                else
                    ChecklistDb.addWithValidation(name)
                onUi { onSuccess(newChecklistDb) }
            } catch (e: UiException) {
                dialogsManager.alert(e.uiMessage)
            }
        }
    }
}
