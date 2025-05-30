package me.timeto.shared.ui.notes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.NoteDb
import me.timeto.shared.launchExIo
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.vm.__Vm

class NoteFormVm(
    noteDb: NoteDb?,
) : __Vm<NoteFormVm.State>() {

    data class State(
        val noteDb: NoteDb?,
        val text: String,
    ) {

        val title: String =
            if (noteDb == null) "New Note" else "Edit Note"

        val doneText = "Save"
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
