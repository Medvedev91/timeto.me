package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.UIException
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.launchEx
import me.timeto.shared.showUiAlert

class ShortcutFormSheetVm(
    val shortcut: ShortcutDb?
) : __Vm<ShortcutFormSheetVm.State>() {

    data class State(
        val headerTitle: String,
        val headerDoneText: String,
        val inputNameValue: String,
        val inputUriValue: String,
    ) {
        val isHeaderDoneEnabled = (inputNameValue.isNotBlank() && inputUriValue.isNotBlank())
        val inputNameHeader = "SHORTCUT NAME"
        val inputNamePlaceholder = "Name"
        val inputUriHeader = "SHORTCUT LINK"
        val inputUriPlaceholder = "Link"
    }

    override val state: MutableStateFlow<State>

    init {
        state = MutableStateFlow(
            State(
                headerTitle = if (shortcut != null) "Edit Shortcut" else "New Shortcut",
                headerDoneText = if (shortcut != null) "Done" else "Create",
                inputNameValue = shortcut?.name ?: "",
                inputUriValue = shortcut?.uri ?: "",
            )
        )
    }

    fun setInputNameValue(text: String) = state.update {
        it.copy(inputNameValue = text)
    }

    fun setInputUriValue(text: String) = state.update {
        it.copy(inputUriValue = text)
    }

    fun setAndroidPackage(androidPackage: String) = state.update {
        it.copy(inputUriValue = "${ShortcutDb.ANDROID_PACKAGE_PREFIX}$androidPackage")
    }

    fun save(
        onSuccess: () -> Unit
    ): Unit = scopeVM().launchEx {
        try {
            if (shortcut != null)
                shortcut.upWithValidation(
                    name = state.value.inputNameValue,
                    uri = state.value.inputUriValue,
                )
            else
                ShortcutDb.addWithValidation(
                    name = state.value.inputNameValue,
                    uri = state.value.inputUriValue,
                )
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
