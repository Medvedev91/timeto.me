package me.timeto.shared

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.vm.__Vm

class WatchTaskSheetVm(
    val task: TaskDb,
) : __Vm<WatchTaskSheetVm.State>() {

    inner class ActivityUI(
        val activity: ActivityDb,
    ) {

        val listTitle = activity.nameWithEmoji().textFeatures().textUi()

        val timerHints = activity.timerHints.map { seconds ->
            TimerHintData(
                seconds = seconds,
                onStart = {
                    WatchToIosSync.startTaskWithLocal(
                        activity = activity,
                        timer = seconds,
                        task = task
                    )
                }
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
}
