package me.timeto.shared.vm.goals.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.vm.Vm

class Goal2FormVm(
    initGoalDb: Goal2Db?,
) : Vm<Goal2FormVm.State>() {

    data class State(
        val initGoalDb: Goal2Db?,
        val name: String,
        val seconds: Int,
        val secondsPickerItemsUi: List<SecondsPickerItemUi>,
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
            seconds.toTimerHintNote(isShort = false)
    }

    override val state: MutableStateFlow<State>

    init {
        val tf = (initGoalDb?.name ?: "").textFeatures()
        val seconds: Int = initGoalDb?.seconds ?: 3_600
        state = MutableStateFlow(
            State(
                initGoalDb = initGoalDb,
                name = tf.textNoFeatures,
                seconds = seconds,
                secondsPickerItemsUi = buildSecondsPickerItems(defSeconds = seconds),
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

    ///

    data class SecondsPickerItemUi(
        val title: String,
        val seconds: Int,
    )
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

        val hours: Int = seconds / 3600
        val minutes: Int = (seconds % 3600) / 60

        val title: String = when {
            hours == 0 -> "$minutes min"
            minutes == 0 -> "$hours h"
            else -> "$hours : ${minutes.toString().padStart(2, '0')}"
        }

        Goal2FormVm.SecondsPickerItemUi(
            title = title,
            seconds = seconds,
        )
    }
}
