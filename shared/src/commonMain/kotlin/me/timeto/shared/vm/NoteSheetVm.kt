package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.NoteDb
import me.timeto.shared.onEachExIn

class NoteSheetVm(
    noteDb: NoteDb,
) : __Vm<NoteSheetVm.State>() {

    data class State(
        val noteDb: NoteDb,
    )

    override val state = MutableStateFlow(
        State(
            noteDb = noteDb,
        )
    )

    init {
        val scopeVm = scopeVm()
        NoteDb.selectAscFlow().onEachExIn(scopeVm) { notesDb ->
            val newNoteDb: NoteDb? =
                notesDb.firstOrNull { it.id == noteDb.id }
            if (newNoteDb != null)
                state.update { it.copy(noteDb = newNoteDb) }
        }
    }
}
