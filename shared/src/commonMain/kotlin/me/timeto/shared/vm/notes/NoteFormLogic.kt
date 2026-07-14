package me.timeto.shared.vm.notes

import me.timeto.shared.db.NoteDb
import me.timeto.shared.db.NoteFolderDb

sealed class NoteFormLogic {

    data class NewNote(
        val noteFolderDb: NoteFolderDb,
    ) : NoteFormLogic()

    data class EditNote(
        val noteDb: NoteDb,
    ) : NoteFormLogic()
}
