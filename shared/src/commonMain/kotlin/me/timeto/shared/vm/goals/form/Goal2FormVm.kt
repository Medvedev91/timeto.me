package me.timeto.shared.vm.goals.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.Vm

class Goal2FormVm(
    initGoalDb: Goal2Db?,
) : Vm<Goal2FormVm.State>() {

    data class State(
        val initGoalDb: Goal2Db?,
        val name: String,
        val seconds: Int,
        val secondsPickerItemsUi: List<SecondsPickerItemUi>,
        val parentGoalsUi: List<GoalUi>,
        val parentGoalUi: GoalUi?,
    ) {

        val title: String =
            if (initGoalDb == null) "New Goal" else "Edit Goal"

        val doneText: String =
            if (initGoalDb == null) "Create" else "Save"
        val isDoneEnabled: Boolean =
            name.isNotBlank()

        val namePlaceholder = "Name"

        val secondsTitle = "Time"
        val secondsNote: String =
            secondsToString(seconds)

        val parentGoalTitle = "Parent Goal"
    }

    override val state: MutableStateFlow<State>

    init {
        val tf = (initGoalDb?.name ?: "").textFeatures()
        val seconds: Int = initGoalDb?.seconds ?: 3_600
        val parentGoalsUi = Cache.goals2Db
            .filter { it.id != initGoalDb?.id }
            .map { GoalUi(it) }
        val parentGoalUi: GoalUi? =
            parentGoalsUi.firstOrNull { it.goalDb.id == initGoalDb?.parent_id }
        state = MutableStateFlow(
            State(
                initGoalDb = initGoalDb,
                name = tf.textNoFeatures,
                seconds = seconds,
                secondsPickerItemsUi = buildSecondsPickerItems(defSeconds = seconds),
                parentGoalsUi = parentGoalsUi,
                parentGoalUi = parentGoalUi,
            )
        )
    }

    ///

    fun setName(newName: String) {
        state.update { it.copy(name = newName) }
    }

    fun setSeconds(newSeconds: Int) {
        state.update { it.copy(seconds = newSeconds) }
    }

    fun setParentGoalUi(goalUi: GoalUi?) {
        state.update { it.copy(parentGoalUi = goalUi) }
    }

    ///

    data class SecondsPickerItemUi(
        val title: String,
        val seconds: Int,
    )

    data class GoalUi(
        val goalDb: Goal2Db,
    ) {
        val title: String =
            goalDb.name.textFeatures().textNoFeatures
    }
}

private fun buildSecondsPickerItems(
    defSeconds: Int,
): List<Goal2FormVm.SecondsPickerItemUi> {

    val a: List<Int> =
        (1..10).map { it * 60 } + // 1 - 10 min by 1 min
                (1..10).map { (600 + (it * 300)) } + // 15 min - 1 hour by 5 min
                (1..138).map { (3_600 + (it * 600)) } + // 1 hour + by 10 min
                defSeconds

    return a.toSet().sorted().map { seconds ->
        Goal2FormVm.SecondsPickerItemUi(
            title = secondsToString(seconds),
            seconds = seconds,
        )
    }
}

private fun secondsToString(seconds: Int): String {
    val hours: Int = seconds / 3600
    val minutes: Int = (seconds % 3600) / 60
    return when {
        hours == 0 -> "$minutes min"
        minutes == 0 -> "$hours hr"
        else -> "$hours hr ${minutes.toString().padStart(2, '0')} min"
    }
}
