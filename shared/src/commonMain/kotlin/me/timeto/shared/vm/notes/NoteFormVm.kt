package me.timeto.shared.vm.notes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.NoteDb
import me.timeto.shared.launchExIo
import me.timeto.shared.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.db.NoteFolderDb
import me.timeto.shared.vm.Vm

class NoteFormVm(
    noteFormLogic: NoteFormLogic,
) : Vm<NoteFormVm.State>() {

    data class State(
        val noteDb: NoteDb?,
        val noteFolderDb: NoteFolderDb,
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
            noteDb = when (noteFormLogic) {
                is NoteFormLogic.NewNote -> null
                is NoteFormLogic.EditNote -> noteFormLogic.noteDb
            },
            noteFolderDb = when (noteFormLogic) {
                is NoteFormLogic.NewNote -> noteFormLogic.noteFolderDb
                is NoteFormLogic.EditNote -> noteFormLogic.noteDb.selectFolderDbCached()
            },
            text = when (noteFormLogic) {
                is NoteFormLogic.NewNote -> ""
                is NoteFormLogic.EditNote -> noteFormLogic.noteDb.text
            },
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
            val noteDb: NoteDb? =
                state.value.noteDb
            val noteFolderDb: NoteFolderDb =
                state.value.noteFolderDb
            val text: String =
                state.value.text
            if (noteDb != null)
                noteDb.updateWithValidation(
                    newText = text,
                    newNoteFolderDb = noteFolderDb,
                )
            else
                NoteDb.insertWithValidation(
                    text = text,
                    noteFolderDb = noteFolderDb,
                )
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
            message = "Are you sure you want to delete \"${noteDb.buildTitle()}\" note?",
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
