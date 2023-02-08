package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.UIException
import timeto.shared.db.TaskFolderModel
import timeto.shared.launchExDefault
import timeto.shared.reportApi
import timeto.shared.showUiAlert

class FolderFormSheetVM(
    val folder: TaskFolderModel?
) : __VM<FolderFormSheetVM.State>() {

    data class State(
        val folder: TaskFolderModel?,
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
    ) = launchExDefault {
        try {
            val name = state.value.inputNameValue
            if (folder != null) {
                folder.upNameWithValidation(name)
            } else {
                TaskFolderModel.addWithValidation(name)
            }
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }

    fun delete(
        onSuccess: () -> Unit
    ) {
        try {
            val folder = folder ?: return reportApi("FolderFormSheetVM no folder. WTF??!!")
            if (folder.isToday)
                throw UIException("It's impossible to delete \"Today\" folder")
            folder.backupable__delete()
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
