package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.NoteModel

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

        val deleteFun: ((onSuccess: () -> Unit) -> Unit)? =
            if (note == null) null else { onSuccess ->
                showUiConfirmation(
                    UIConfirmationData(
                        text = "Are you sure you want to delete \"${note.title}\" note?",
                        buttonText = "Delete",
                        isRed = true,
                    ) {
                        launchExDefault {
                            try {
                                note.delete()
                                onSuccess()
                            } catch (e: UIException) {
                                showUiAlert(e.uiMessage)
                            }
                        }
                    }
                )
            }
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
