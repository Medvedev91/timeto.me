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
        state = MutableStateFlow(
            State(
                initGoalDb = initGoalDb,
                name = tf.textNoFeatures,
                seconds = initGoalDb?.seconds ?: 3_600
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
}
