package me.timeto.shared

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.db.TaskDb
import me.timeto.shared.vm.Vm

class WatchTaskSheetVm(
    val taskDb: TaskDb,
) : Vm<WatchTaskSheetVm.State>() {

    data class State(
        val goalsUi: List<GoalUi>,
    )

    override val state = MutableStateFlow(
        State(
            goalsUi = Cache.goals2Db.map { activity ->
                GoalUi(
                    goalDb = activity,
                )
            },
        )
    )

    ///

    inner class GoalUi(
        val goalDb: Goal2Db,
    ) {

        val listTitle: String =
            goalDb.name.textFeatures().textUi()

        val timerHintsUi: List<TimerHintUi> = listOf(5 * 60, 15 * 60, 45 * 60).map { seconds ->
            TimerHintUi(
                seconds = seconds,
                onStart = {
                    WatchToIosSync.startTaskWithLocal(
                        taskDb = taskDb,
                        goalDb = goalDb,
                        timer = seconds,
                    )
                },
            )
        }

        fun onTap() {
            WatchToIosSync.startTaskWithLocal(
                taskDb = taskDb,
                goalDb = goalDb,
                timer = null,
            )
        }
    }

    class TimerHintUi(
        val seconds: Int,
        val onStart: suspend () -> Unit,
    ) {

        val text: String =
            seconds.toTimerHintNote(isShort = true)

        fun startInterval() {
            launchExIo {
                onStart()
            }
        }
    }
}
