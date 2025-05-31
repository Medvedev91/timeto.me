package me.timeto.shared.vm.goals.form

sealed class GoalFormStrategy {

    class NewFormData(
        val onDone: (GoalFormData) -> Unit,
    ) : GoalFormStrategy()

    class EditFormData(
        val initGoalFormData: GoalFormData,
        val onDone: (GoalFormData) -> Unit,
        val onDelete: () -> Unit,
    ) : GoalFormStrategy()
}
