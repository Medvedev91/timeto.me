package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.UIException
import me.timeto.shared.db.NoteModel
import me.timeto.shared.launchEx
import me.timeto.shared.showUiAlert

class NoteFormSheetVM(
    val note: NoteModel?,
) : __VM<NoteFormSheetVM.State>() {

    data class State(
        val note: NoteModel?,
        val inputTextValue: String,
    ) {

        val headerTitle = if (note == null) "New Note" else "Edit Note"
        val doneTitle = "Save"

        val inputTextPlaceholder = "Text"
    }

    override val state = MutableStateFlow(
        State(
            note = note,
            inputTextValue = note?.text ?: "",
        )
    )

    fun setInputText(newText: String) = state.update {
        it.copy(inputTextValue = newText)
    }

    fun save(
        onSuccess: () -> Unit
    ): Unit = scopeVM().launchEx {
        try {
            val text = state.value.inputTextValue
            if (note != null)
                note.upWithValidation(
                    newText = text,
                )
            else
                NoteModel.addWithValidation(
                    text = text,
                )
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
