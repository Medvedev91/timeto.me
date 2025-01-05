package me.timeto.shared.vm.ChecklistForm

import kotlinx.coroutines.flow.*
import me.timeto.shared.UIException
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.launchEx
import me.timeto.shared.misc.DialogsManager
import me.timeto.shared.vm.__Vm

class ChecklistFormSettingsVm(
    checklistDb: ChecklistDb?,
) : __Vm<ChecklistFormSettingsVm.State>() {

    data class State(
        val checklistDb: ChecklistDb?,
        val name: String,
    ) {

        val title: String = if (checklistDb != null) "Edit Checklist" else "New Checklist"
        val isSaveEnabled: Boolean = name.isNotBlank()

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
    ) {
        scopeVm().launchEx {
            try {
                val oldChecklistDb: ChecklistDb? = state.value.checklistDb
                val newChecklistDb: ChecklistDb = if (oldChecklistDb != null)
                    oldChecklistDb.upNameWithValidation(state.value.name)
                else
                    ChecklistDb.addWithValidation(state.value.name)
                onSuccess(newChecklistDb)
            } catch (e: UIException) {
                dialogsManager.alert(e.uiMessage)
            }
        }
    }
}
