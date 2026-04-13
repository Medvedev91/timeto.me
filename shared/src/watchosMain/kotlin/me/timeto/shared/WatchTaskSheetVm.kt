package me.timeto.shared

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.vm.Vm

class WatchTaskSheetVm(
    val taskDb: TaskDb,
) : Vm<WatchTaskSheetVm.State>() {

    data class State(
        val activitiesUi: List<ActivityUi>,
    )

    override val state = MutableStateFlow(
        State(
            activitiesUi = Cache.activitiesDb.map { activityDb ->
                ActivityUi(
                    activityDb = activityDb,
                )
            },
        )
    )

    ///

    inner class ActivityUi(
        val activityDb: ActivityDb,
    ) {

        val listTitle: String =
            activityDb.name.textFeatures().textUi()

        val timerHintsUi: List<TimerHintUi> = listOf(5 * 60, 15 * 60, 45 * 60).map { seconds ->
            TimerHintUi(
                seconds = seconds,
                onStart = {
                    WatchToIosSync.startTaskWithLocal(
                        taskDb = taskDb,
                        activityDb = activityDb,
                        timer = seconds,
                    )
                },
            )
        }

        fun onTap() {
            WatchToIosSync.startTaskWithLocal(
                taskDb = taskDb,
                activityDb = activityDb,
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
