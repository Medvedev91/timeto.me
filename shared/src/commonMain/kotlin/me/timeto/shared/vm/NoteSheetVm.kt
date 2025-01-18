package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.NoteDb
import me.timeto.shared.onEachExIn

class NoteSheetVm(
    val note: NoteDb,
) : __Vm<NoteSheetVm.State>() {

    data class State(
        val note: NoteDb,
    )

    override val state = MutableStateFlow(
        State(
            note = note,
        )
    )

    override fun onAppear() {
        val scope = scopeVm()
        NoteDb.selectAscFlow().onEachExIn(scope) { notes ->
            // Null on deletion
            val newNote = notes.firstOrNull { it.id == note.id }
            if (newNote != null)
                state.update { it.copy(note = newNote) }
        }
    }
}
