package me.timeto.shared.ui.shortcuts

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.launchExIo
import me.timeto.shared.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.ui.__Vm

class ShortcutFormVm(
    shortcutDb: ShortcutDb?,
) : __Vm<ShortcutFormVm.State>() {

    data class State(
        val shortcutDb: ShortcutDb?,
        val name: String,
        val uri: String,
    ) {

        val title: String =
            if (shortcutDb != null) "Edit Shortcut" else "New Shortcut"

        val doneText: String =
            if (shortcutDb != null) "Save" else "Create"

        val isSaveEnabled: Boolean =
            (name.isNotBlank() && uri.isNotBlank())

        val deleteText = "Delete Shortcut"

        val nameHeader = "SHORTCUT NAME"
        val namePlaceholder = "Name"

        val uriHeader = "SHORTCUT LINK"
        val uriPlaceholder = "Link"
    }

    override val state = MutableStateFlow(
        State(
            shortcutDb = shortcutDb,
            name = shortcutDb?.name ?: "",
            uri = shortcutDb?.uri ?: "",
        )
    )

    fun setName(name: String) {
        state.update { it.copy(name = name) }
    }

    fun setUri(uri: String) {
        state.update { it.copy(uri = uri) }
    }

    fun prepUriForAndroidPackage(androidPackage: String): String {
        return "${ShortcutDb.ANDROID_PACKAGE_PREFIX}$androidPackage"
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: (ShortcutDb) -> Unit,
    ): Unit = launchExIo {
        try {
            val name: String = state.value.name
            val uri: String = state.value.uri
            val oldShortcutDb: ShortcutDb? = state.value.shortcutDb
            val newShortcutDb: ShortcutDb = if (oldShortcutDb != null)
                oldShortcutDb.updateWithValidation(name = name, uri = uri)
            else
                ShortcutDb.insertWithValidation(name = name, uri = uri)
            onUi { onSuccess(newShortcutDb) }
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }

    fun delete(
        shortcutDb: ShortcutDb,
        dialogsManager: DialogsManager,
        onDelete: () -> Unit,
    ) {
        dialogsManager.confirmation(
            message = "Are you sure you want to delete \"${shortcutDb.name}\" shortcut?",
            buttonText = "Delete",
            onConfirm = {
                launchExIo {
                    shortcutDb.delete()
                    onUi { onDelete() }
                }
            },
        )
    }
}
