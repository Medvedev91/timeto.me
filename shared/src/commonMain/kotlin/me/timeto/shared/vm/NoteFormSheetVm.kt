package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.NoteDb
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.ui.UiException

class NoteFormSheetVm(
    noteDb: NoteDb?,
) : __Vm<NoteFormSheetVm.State>() {

    data class State(
        val noteDb: NoteDb?,
        val text: String,
    ) {

        val title: String =
            if (noteDb == null) "New Note" else "Edit Note"

        val saveText = "Save"
        val isSaveEnabled: Boolean = text.isNotBlank()

        val textPlaceholder = "Text"
    }

    override val state = MutableStateFlow(
        State(
            noteDb = noteDb,
            text = noteDb?.text ?: "",
        )
    )

    fun setText(text: String) {
        state.update { it.copy(text = text) }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ): Unit = launchExIo {
        try {
            val noteDb: NoteDb? = state.value.noteDb
            val text: String = state.value.text
            if (noteDb != null)
                noteDb.updateWithValidation(newText = text)
            else
                NoteDb.insertWithValidation(text = text)
            onUi { onSuccess() }
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }

    fun delete(
        noteDb: NoteDb,
        dialogsManager: DialogsManager,
        onDelete: () -> Unit,
    ) {
        dialogsManager.confirmation(
            message = "Are you sure you want to delete \"${noteDb.title}\" note?",
            buttonText = "Delete",
            onConfirm = {
                launchExIo {
                    noteDb.delete()
                    onUi { onDelete() }
                }
            },
        )
    }
}
