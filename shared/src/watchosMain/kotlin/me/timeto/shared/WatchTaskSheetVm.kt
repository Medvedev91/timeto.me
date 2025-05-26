package me.timeto.shared

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.ui.TimerHintData
import me.timeto.shared.vm.__Vm

class WatchTaskSheetVm(
    val task: TaskDb,
) : __Vm<WatchTaskSheetVm.State>() {

    inner class ActivityUI(
        val activity: ActivityDb,
    ) {

        val listTitle: String =
            activity.nameWithEmoji().textFeatures().textUi()

        val timerHintsUi: List<TimerHintUi> = activity.timerHints.map { seconds ->
            TimerHintUi(
                seconds = seconds,
                onStart = {
                    WatchToIosSync.startTaskWithLocal(
                        activity = activity,
                        timer = seconds,
                        task = task
                    )
                },
            )
        }
    }

    data class State(
        val activitiesUI: List<ActivityUI>,
    )

    override val state = MutableStateFlow(
        State(
            activitiesUI = Cache.activitiesDbSorted.map { activity ->
                ActivityUI(
                    activity = activity,
                )
            },
        )
    )

    ///

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
