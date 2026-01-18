package me.timeto.shared.vm.tasks

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.Cache
import me.timeto.shared.DayBarsUi
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.db.TaskDb
import me.timeto.shared.launchExIo
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.vm.Vm

class TaskTimerVm(
    taskDb: TaskDb,
) : Vm<TaskTimerVm.State>() {

    data class State(
        val goalsUi: List<GoalUi>
    )

    // todo update list on changes
    override val state = MutableStateFlow(
        State(
            goalsUi = Cache.goals2Db.map { goalDb ->
                GoalUi(
                    goalDb = goalDb,
                    taskDb = taskDb,
                )
            },
        )
    )

    ///

    class GoalUi(
        val goalDb: Goal2Db,
        val taskDb: TaskDb,
    ) {

        val text: String =
            goalDb.name.textFeatures().textUi()

        val timerHintsUi: List<TimerHintUi> =
            goalDb.buildTimerHintsOrDefault().map { seconds ->
                TimerHintUi(
                    seconds = seconds,
                    onTap = {
                        launchExIo {
                            taskDb.startInterval(
                                timer = seconds,
                                goalDb = goalDb,
                            )
                        }
                    },
                )
            }

        fun onTap() {
            launchExIo {
                val goalStats = DayBarsUi.buildToday().buildGoalStats(goalDb)
                taskDb.startInterval(
                    timer = goalStats.calcTimer(),
                    goalDb = goalDb,
                )
            }
        }
    }

    class TimerHintUi(
        val seconds: Int,
        val onTap: () -> Unit,
    ) {
        val title: String =
            seconds.toTimerHintNote(isShort = true)
    }
}
