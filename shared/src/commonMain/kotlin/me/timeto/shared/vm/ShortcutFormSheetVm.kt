package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.launchEx
import me.timeto.shared.misc.DialogsManager
import me.timeto.shared.misc.UiException

class ShortcutFormSheetVm(
    private val shortcutDb: ShortcutDb?,
) : __Vm<ShortcutFormSheetVm.State>() {

    data class State(
        val title: String,
        val saveText: String,
        val name: String,
        val uri: String,
    ) {
        val isSaveEnabled = (name.isNotBlank() && uri.isNotBlank())
        val nameHeader = "SHORTCUT NAME"
        val namePlaceholder = "Name"
        val uriHeader = "SHORTCUT LINK"
        val uriPlaceholder = "Link"
    }

    override val state = MutableStateFlow(
        State(
            title = if (shortcutDb != null) "Edit Shortcut" else "New Shortcut",
            saveText = if (shortcutDb != null) "Save" else "Create",
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

    fun setAndroidPackage(androidPackage: String) {
        state.update {
            it.copy(uri = "${ShortcutDb.ANDROID_PACKAGE_PREFIX}$androidPackage")
        }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: (ShortcutDb) -> Unit,
    ): Unit = scopeVm().launchEx {
        try {
            val name: String = state.value.name
            val uri: String = state.value.name
            val newShortcutDb: ShortcutDb = if (shortcutDb != null)
                shortcutDb.updateWithValidation(name = name, uri = uri)
            else
                ShortcutDb.insertWithValidation(name = name, uri = uri)
            onUi { onSuccess(newShortcutDb) }
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }
}
