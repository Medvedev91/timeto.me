package me.timeto.shared.vm.note_folder_form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.DialogsManager
import me.timeto.shared.Symbol
import me.timeto.shared.UiException
import me.timeto.shared.db.NoteDb
import me.timeto.shared.db.NoteFolderDb
import me.timeto.shared.launchExIo
import me.timeto.shared.vm.Vm

class NoteFolderFormVm(
    noteFolderDb: NoteFolderDb?,
) : Vm<NoteFolderFormVm.State>() {

    data class State(
        val noteFolderDb: NoteFolderDb?,
        val onHome: Boolean,
        val symbol: Symbol?,
        val name: String,
    ) {

        val title: String =
            if (noteFolderDb != null) "Edit Folder" else "New Folder"

        val doneText: String =
            if (noteFolderDb != null) "Done" else "Create"

        val namePlaceholder = "Folder Name"
        val isSaveEnabled: Boolean =
            name.isNotBlank()

        val iconTitle = "Icon"
        val onHomeTitle = "Display on Home Screen"

        val deleteText = "Delete Folder"
    }

    override val state = MutableStateFlow(
        State(
            noteFolderDb = noteFolderDb,
            onHome = noteFolderDb?.onHome ?: true,
            symbol = noteFolderDb?.symbolOrDefault(),
            name = noteFolderDb?.name ?: "",
        )
    )

    fun setOnHome(onHome: Boolean) {
        state.update { it.copy(onHome = onHome) }
    }

    fun setSymbol(symbol: Symbol) {
        state.update { it.copy(symbol = symbol) }
    }

    fun setName(name: String) {
        state.update { it.copy(name = name) }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ): Unit = launchExIo {
        try {
            val onHome: Boolean =
                state.value.onHome
            val symbol: Symbol = state.value.symbol ?: run {
                dialogsManager.alert("No Symbol")
                return@launchExIo
            }
            val name: String =
                state.value.name
            val noteFolderDb: NoteFolderDb? =
                state.value.noteFolderDb
            if (noteFolderDb != null)
                noteFolderDb.updateWithValidation(
                    onHome = onHome,
                    symbol = symbol,
                    rawName = name,
                )
            else
                NoteFolderDb.insertWithValidation(
                    onHome = onHome,
                    symbol = symbol,
                    rawName = name,
                )
            onUi { onSuccess() }
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }

    fun delete(
        noteFolderDb: NoteFolderDb,
        dialogsManager: DialogsManager,
        onDelete: () -> Unit,
    ): Unit = launchExIo {

        if (NoteDb.selectAllSorted().any { it.folderId == noteFolderDb.id }) {
            dialogsManager.alert("The folder must be empty before deletion")
            return@launchExIo
        }

        dialogsManager.confirmation(
            message = "Are you sure you want to delete \"${noteFolderDb.name}\" folder",
            buttonText = "Delete",
        ) {
            launchExIo {
                noteFolderDb.delete()
                onUi { onDelete() }
            }
        }
    }
}
