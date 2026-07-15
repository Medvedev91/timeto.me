package me.timeto.shared.vm.notes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.NoteFolderDb
import me.timeto.shared.onEachExIn
import me.timeto.shared.vm.Vm

class NotesScreenVm(
    initNoteFolderDb: NoteFolderDb,
) : Vm<NotesScreenVm.State>() {

    data class State(
        val noteFolderDb: NoteFolderDb,
    ) {
        val title: String =
            noteFolderDb.name
    }

    override val state = MutableStateFlow(
        State(
            noteFolderDb = initNoteFolderDb,
        )
    )

    init {
        val scopeVm = scopeVm()
        NoteFolderDb.selectAllSortedFlow().onEachExIn(scopeVm) { noteFoldersDb ->
            val noteFolderDb: NoteFolderDb? =
                noteFoldersDb.firstOrNull { it.id == initNoteFolderDb.id }
            // Null After Deletion
            if (noteFolderDb != null)
                state.update { it.copy(noteFolderDb = noteFolderDb) }
        }
    }
}
