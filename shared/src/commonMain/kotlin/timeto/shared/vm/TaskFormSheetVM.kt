package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.*
import timeto.shared.db.TaskModel

class TaskFormSheetVM(
    val task: TaskModel,
) : __VM<TaskFormSheetVM.State>() {

    data class State(
        val headerTitle: String,
        val headerDoneText: String,
        val textFeatures: TextFeatures,
    ) {
        val inputTextValue = textFeatures.textNoFeatures
        val isHeaderDoneEnabled = inputTextValue.isNotBlank()
    }

    override val state: MutableStateFlow<State>

    init {
        val textFeatures = TextFeatures.parse(task.text)
        state = MutableStateFlow(
            State(
                headerTitle = "Edit Task",
                headerDoneText = "Done",
                textFeatures = textFeatures,
            )
        )
    }

    fun setInputTextValue(text: String) = state.update {
        it.copy(textFeatures = it.textFeatures.copy(textNoFeatures = text))
    }

    fun setTriggers(newTriggers: List<Trigger>) = state.update {
        it.copy(textFeatures = it.textFeatures.copy(triggers = newTriggers))
    }

    fun save(
        onSuccess: () -> Unit,
    ) = scopeVM().launchEx {
        try {
            // todo check if a text without features
            val textWithFeatures = state.value.textFeatures.textWithFeatures()
            task.upTextWithValidation(textWithFeatures)
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
