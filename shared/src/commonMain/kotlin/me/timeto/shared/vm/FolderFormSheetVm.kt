package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.ui.UiException

class FolderFormSheetVm(
    folderDb: TaskFolderDb?,
) : __Vm<FolderFormSheetVm.State>() {

    data class State(
        val folderDb: TaskFolderDb?,
        val name: String,
    ) {

        val title: String =
            if (folderDb != null) "Edit Folder" else "New Folder"

        val saveText: String =
            if (folderDb != null) "Done" else "Create"

        val namePlaceholder = "Folder Name"
        val isSaveEnabled: Boolean = name.isNotBlank()
        val deleteText = "Delete Folder"
    }

    override val state = MutableStateFlow(
        State(
            folderDb = folderDb,
            name = folderDb?.name ?: "",
        )
    )

    fun setName(name: String) {
        state.update { it.copy(name = name) }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ): Unit = launchExIo {
        try {
            val name: String = state.value.name
            val folderDb: TaskFolderDb? = state.value.folderDb
            if (folderDb != null)
                folderDb.updateNameWithValidation(name)
            else
                TaskFolderDb.insertWithValidation(name)
            onUi { onSuccess() }
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }

    fun delete(
        folderDb: TaskFolderDb,
        dialogsManager: DialogsManager,
        onDelete: () -> Unit,
    ): Unit = launchExIo {

        if (folderDb.isToday) {
            dialogsManager.alert("It's impossible to delete \"Today\" folder")
            return@launchExIo
        }

        if (TaskDb.getAsc().any { it.folder_id == folderDb.id }) {
            dialogsManager.alert("The folder must be empty before deletion")
            return@launchExIo
        }

        dialogsManager.confirmation(
            message = "Are you sure you want to delete \"${folderDb.name}\" folder",
            buttonText = "Delete",
        ) {
            launchExIo {
                folderDb.delete()
                onUi { onDelete() }
            }
        }
    }
}
