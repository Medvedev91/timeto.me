package me.timeto.shared.vm.tasks.folders

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.TaskDb
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.launchExIo
import me.timeto.shared.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.Vm

class TaskFolderFormVm(
    folderDb: TaskFolderDb?,
) : Vm<TaskFolderFormVm.State>() {

    data class State(
        val folderDb: TaskFolderDb?,
        val activityDb: ActivityDb?,
        val name: String,
    ) {

        val title: String =
            if (folderDb != null) "Edit Folder" else "New Folder"

        val doneText: String =
            if (folderDb != null) "Done" else "Create"

        val namePlaceholder = "Folder Name"
        val isSaveEnabled: Boolean =
            name.isNotBlank()

        val isActivityAvailable: Boolean = when {
            folderDb == null -> true
            folderDb.isToday || folderDb.isTmrw -> false
            else -> true
        }

        val activityTitle: String = "Activity"
        val activityNote: String? =
            activityDb?.name?.textFeatures()?.textNoFeatures

        val activitiesUi: List<ActivityUi> =
            Cache.activitiesDb.map { ActivityUi(it) }

        val deleteText = "Delete Folder"
    }

    override val state = MutableStateFlow(
        State(
            folderDb = folderDb,
            activityDb = folderDb?.selectActivityDbOrNullCached(),
            name = folderDb?.name ?: "",
        )
    )

    fun setName(name: String) {
        state.update { it.copy(name = name) }
    }

    fun setActivity(activityDb: ActivityDb?) {
        state.update { it.copy(activityDb = activityDb) }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ): Unit = launchExIo {
        try {
            val activityDb: ActivityDb? =
                state.value.activityDb
            val name: String =
                state.value.name
            val folderDb: TaskFolderDb? =
                state.value.folderDb
            if (folderDb != null)
                folderDb.update(
                    sort = folderDb.sort,
                    activityDb = activityDb,
                    rawName = name,
                )
            else
                TaskFolderDb.insertWithValidation(
                    rawName = name,
                    activityDb = activityDb,
                )
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

        if (TaskDb.selectAsc().any { it.folder_id == folderDb.id }) {
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

    ///

    data class ActivityUi(
        val activityDb: ActivityDb,
    ) {
        val title: String =
            activityDb.name.textFeatures().textNoFeatures
    }
}
