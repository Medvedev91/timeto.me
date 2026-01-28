package me.timeto.shared.vm.checklists.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.launchEx
import me.timeto.shared.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.Vm

class ChecklistFormItemVm(
    checklistDb: ChecklistDb,
    checklistItemDb: ChecklistItemDb?,
) : Vm<ChecklistFormItemVm.State>() {

    data class State(
        val checklistDb: ChecklistDb,
        val checklistItemDb: ChecklistItemDb?,
        val nestedChecklistsDb: List<ChecklistDb>,
        val nestedShortcutsDb: List<ShortcutDb>,
        val text: String,
    ) {

        val title: String =
            if (checklistItemDb != null) "Edit" else "New Item"

        val saveButtonText = "Save"
        val isSaveEnabled: Boolean = text.isNotBlank()

        val checklistsNote: String =
            nestedChecklistsDb.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: "None"

        val shortcutsNote: String =
            nestedShortcutsDb.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: "None"
    }

    override val state: MutableStateFlow<State>

    init {
        val textFeatures = (checklistItemDb?.text ?: "").textFeatures()
        state = MutableStateFlow(
            State(
                checklistDb = checklistDb,
                checklistItemDb = checklistItemDb,
                nestedChecklistsDb = textFeatures.checklistsDb,
                nestedShortcutsDb = textFeatures.shortcutsDb,
                text = textFeatures.textNoFeatures,
            )
        )
    }

    fun setText(text: String) {
        state.update { it.copy(text = text) }
    }

    fun setNestedChecklists(newNestedChecklistsDb: List<ChecklistDb>) {
        state.update { it.copy(nestedChecklistsDb = newNestedChecklistsDb) }
    }

    fun setNestedShortcuts(newNestedShortcuts: List<ShortcutDb>) {
        state.update { it.copy(nestedShortcutsDb = newNestedShortcuts) }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ): Unit = scopeVm().launchEx {
        try {
            val state = state.value
            val textWithFeatures: String = state.text.textFeatures().copy(
                checklistsDb = state.nestedChecklistsDb,
                shortcutsDb = state.nestedShortcutsDb,
            ).textWithFeatures()
            val checklistDb: ChecklistDb = state.checklistDb
            val oldItemDb: ChecklistItemDb? = state.checklistItemDb
            if (oldItemDb != null)
                oldItemDb.updateTextWithValidation(textWithFeatures)
            else
                ChecklistItemDb.insertWithValidation(textWithFeatures, checklistDb, false)
            onUi { onSuccess() }
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }
}
