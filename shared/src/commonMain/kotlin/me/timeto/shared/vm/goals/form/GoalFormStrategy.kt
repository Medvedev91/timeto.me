package me.timeto.shared.vm.goals.form

import me.timeto.shared.db.GoalDb

sealed class GoalFormStrategy {

    class NewFormData(
        val onDone: (GoalFormData) -> Unit,
    ) : GoalFormStrategy()

    class EditFormData(
        val initGoalFormData: GoalFormData,
        val onDone: (GoalFormData) -> Unit,
        val onDelete: () -> Unit,
    ) : GoalFormStrategy()

    class EditGoal(
        val goalDb: GoalDb,
    ) : GoalFormStrategy()
}
