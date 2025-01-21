package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.ui.UiException

class FolderFormSheetVm(
    val folder: TaskFolderDb?
) : __Vm<FolderFormSheetVm.State>() {

    data class State(
        val folder: TaskFolderDb?,
        val inputNameValue: String,
    ) {
        val headerTitle = if (folder != null) "Edit Folder" else "New Folder"
        val headerDoneText = if (folder != null) "Done" else "Create"
        val inputNameHeader = "FOLDER NAME"
        val inputNamePlaceholder = "Folder Name"
        val isHeaderDoneEnabled = inputNameValue.isNotBlank()
        val deleteFolderText = "Delete Folder"
    }

    override val state = MutableStateFlow(
        State(
            folder = folder,
            inputNameValue = folder?.name ?: "",
        )
    )

    fun setInputNameValue(name: String) = state.update {
        it.copy(inputNameValue = name)
    }

    fun save(
        onSuccess: () -> Unit
    ): Unit = launchExIo {
        try {
            val name = state.value.inputNameValue
            if (folder != null) {
                folder.updateNameWithValidation(name)
            } else {
                TaskFolderDb.insertWithValidation(name)
            }
            onUi { onSuccess() }
        } catch (e: UiException) {
            showUiAlert(e.uiMessage)
        }
    }

    fun delete(
        onSuccess: () -> Unit
    ) = launchExDefault {
        try {
            val folder = folder
            if (folder == null) {
                reportApi("FolderFormSheetVM no folder. WTF??!!")
                return@launchExDefault
            }

            if (folder.isToday)
                throw UIException("It's impossible to delete \"Today\" folder")

            if (TaskDb.getAsc().any { it.folder_id == folder.id })
                throw UIException("The folder must be empty before deletion")

            showUiConfirmation(
                UIConfirmationData(
                    text = "Are you sure you want to delete \"${folder.name}\" folder",
                    buttonText = "Delete",
                    isRed = true,
                ) {
                    launchExDefault {
                        try {
                            folder.backupable__delete()
                            onSuccess()
                        } catch (e: UIException) {
                            showUiAlert(e.uiMessage)
                        }
                    }
                }
            )
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
