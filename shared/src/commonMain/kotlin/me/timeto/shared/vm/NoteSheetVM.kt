package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.NoteModel
import me.timeto.shared.onEachExIn

class NoteSheetVM(
    val note: NoteModel,
) : __VM<NoteSheetVM.State>() {

    data class State(
        val note: NoteModel,
    )

    override val state = MutableStateFlow(
        State(
            note = note,
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        NoteModel.getAscFlow().onEachExIn(scope) { notes ->
            // Null on deletion
            val newNote = notes.firstOrNull { it.id == note.id }
            if (newNote != null)
                state.update { it.copy(note = newNote) }
        }
    }
}
