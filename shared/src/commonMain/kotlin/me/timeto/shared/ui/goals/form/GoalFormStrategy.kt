package me.timeto.shared.ui.goals.form

sealed class GoalFormStrategy {

    class FormData(
        val initGoalFormData: GoalFormData?,
        val onDone: (GoalFormData) -> Unit,
    ) : GoalFormStrategy()
}
