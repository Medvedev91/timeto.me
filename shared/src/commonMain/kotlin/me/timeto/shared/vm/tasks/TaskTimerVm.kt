package me.timeto.shared.vm.tasks

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.Cache
import me.timeto.shared.DayBarsUi
import me.timeto.shared.DaytimeUi
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.launchExIo
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.vm.Vm

class TaskTimerVm(
    taskDb: TaskDb,
) : Vm<TaskTimerVm.State>() {

    data class State(
        val activitiesUi: List<ActivityUi>
    )

    // todo update list on changes
    override val state = MutableStateFlow(
        State(
            activitiesUi = Cache.activitiesDb.map { activityDb ->
                ActivityUi(
                    activityDb = activityDb,
                    taskDb = taskDb,
                )
            },
        )
    )

    ///

    class ActivityUi(
        val activityDb: ActivityDb,
        val taskDb: TaskDb,
    ) {

        val text: String =
            activityDb.name.textFeatures().textUi()

        val timerHintsUi: List<TimerHintUi> =
            activityDb.buildTimerHintsOrDefault().map { seconds ->
                TimerHintUi(
                    seconds = seconds,
                    onTap = {
                        launchExIo {
                            taskDb.startTimer(
                                seconds = seconds,
                                activityDb = activityDb,
                            )
                        }
                    },
                )
            }

        fun start(timer: Int) {
            launchExIo {
                taskDb.startTimer(
                    seconds = timer,
                    activityDb = activityDb,
                )
            }
        }

        fun startUntil(daytimeUi: DaytimeUi) {
            launchExIo {
                taskDb.startTimer(
                    seconds = daytimeUi.calcTimer().seconds,
                    activityDb = activityDb,
                )
            }
        }

        fun startRestOfGoal() {
            launchExIo {
                val activityStats = DayBarsUi.buildToday().buildActivityStats(activityDb)
                taskDb.startTimer(
                    seconds = activityStats.calcRestOfGoal(),
                    activityDb = activityDb,
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
