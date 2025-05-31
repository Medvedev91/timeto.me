package me.timeto.shared.vm.notes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.NoteDb
import me.timeto.shared.onEachExIn
import me.timeto.shared.vm.__Vm

class NoteVm(
    noteDb: NoteDb,
) : __Vm<NoteVm.State>() {

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
