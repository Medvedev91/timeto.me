package me.timeto.shared.ui.goals.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.GoalDb
import me.timeto.shared.textFeatures
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.vm.__Vm

class GoalFormVm(
    private val strategy: GoalFormStrategy,
) : __Vm<GoalFormVm.State>() {

    data class State(
        val strategy: GoalFormStrategy,
        val note: String,
    ) {

        val title: String = when (strategy) {
            is GoalFormStrategy.FormData ->
                if (strategy.initGoalFormData != null) "Edit Goal" else "New Goal"
        }

        val doneText: String = when (strategy) {
            is GoalFormStrategy.FormData -> "Done"
        }

        val notePlaceholder = "Note (optional)"

        ///

        fun buildFormDataOrNull(
            dialogsManager: DialogsManager?,
            goalDb: GoalDb?,
        ): GoalFormData? {
            val validatedData: ValidatedData = validateData(
                dialogsManager = dialogsManager,
            )
            TODO()
//            return GoalFormData(
//                goalDb = goalDb,
//                seconds =,
//                period =,
//                note =,
//                finishText =,
//            )
        }

        private fun validateData(
            dialogsManager: DialogsManager?,
        ): ValidatedData {
            val tf: TextFeatures = note.textFeatures()
            return ValidatedData(
                note = tf.textWithFeatures(),
            )
        }
    }

    override val state: MutableStateFlow<State>

    init {
        val tf: TextFeatures = when (strategy) {
            is GoalFormStrategy.FormData ->
                (strategy.initGoalFormData?.note ?: "").textFeatures()
        }
        state = MutableStateFlow(
            State(
                strategy = strategy,
                note = when (strategy) {
                    is GoalFormStrategy.FormData -> tf.textNoFeatures
                },
            )
        )
    }

    ///

    fun setNote(newNote: String) {
        state.update { it.copy(note = newNote) }
    }
}

///

private data class ValidatedData(
    val note: String,
)
