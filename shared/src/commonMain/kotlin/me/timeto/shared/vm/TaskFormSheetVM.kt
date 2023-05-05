package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.*
import timeto.shared.db.TaskModel

class TaskFormSheetVM(
    val task: TaskModel?,
) : __VM<TaskFormSheetVM.State>() {

    data class State(
        val headerTitle: String,
        val headerDoneText: String,
        val textFeatures: TextFeatures,
    ) {
        val inputTextValue = textFeatures.textNoFeatures
        val isHeaderDoneEnabled = inputTextValue.isNotBlank()
    }

    override val state = MutableStateFlow(
        State(
            headerTitle = if (task != null) "Edit Task" else "New Task",
            headerDoneText = if (task != null) "Done" else "Create",
            textFeatures = (task?.text ?: "").textFeatures(),
        )
    )

    fun setInputTextValue(text: String) = state.update {
        it.copy(textFeatures = it.textFeatures.copy(textNoFeatures = text))
    }

    fun setTextFeatures(newTextFeatures: TextFeatures) = state.update {
        it.copy(textFeatures = newTextFeatures)
    }

    fun save(
        onSuccess: () -> Unit,
    ) = scopeVM().launchEx {
        try {
            // todo check if the text without features
            val textWithFeatures = state.value.textFeatures.textWithFeatures()
            if (task != null) {
                task.upTextWithValidation(textWithFeatures)
            } else {
                TaskModel.addWithValidation(textWithFeatures, DI.getTodayFolder())
            }
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
