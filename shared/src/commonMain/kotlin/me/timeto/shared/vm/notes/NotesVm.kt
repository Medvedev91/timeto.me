package me.timeto.shared.vm.notes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.NoteDb
import me.timeto.shared.db.NoteFolderDb
import me.timeto.shared.onEachExIn
import me.timeto.shared.vm.Vm

class NotesVm(
    noteFolderDb: NoteFolderDb,
) : Vm<NotesVm.State>() {

    data class State(
        val notesUi: List<NoteUi>,
    )

    override val state = MutableStateFlow(
        State(
            notesUi = Cache.notesDb
                .filter { it.folderId == noteFolderDb.id }
                .map { NoteUi(it) },
        )
    )

    init {
        val scopeVm = scopeVm()
        NoteDb.selectAllSortedFlow().onEachExIn(scopeVm) { notesDb ->
            val notesUi: List<NoteUi> =
                notesDb.filter { it.folderId == noteFolderDb.id }.map { NoteUi(it) }
            state.update { it.copy(notesUi = notesUi) }
        }
    }

    ///

    data class NoteUi(
        val noteDb: NoteDb,
    ) {
        val text: String =
            noteDb.buildTitle()
    }
}
