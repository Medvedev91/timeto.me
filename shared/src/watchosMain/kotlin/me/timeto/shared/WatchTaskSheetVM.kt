package me.timeto.shared

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.vm.__VM

class WatchTaskSheetVM(
    val task: TaskDb,
) : __VM<WatchTaskSheetVM.State>() {

    inner class ActivityUI(
        val activity: ActivityDb,
    ) {

        val listTitle = activity.nameWithEmoji().textFeatures().textUi()

        val timerHints = activity.data.timer_hints.getTimerHintsUI(
            historyLimit = 4,
            customLimit = 4,
        ) { hintUI ->
            WatchToIosSync.startTaskWithLocal(
                activity = activity,
                timer = hintUI.seconds,
                task = task
            )
        }
    }

    data class State(
        val activitiesUI: List<ActivityUI>,
    )

    override val state = MutableStateFlow(
        State(
            activitiesUI = DI.activitiesSorted.map { activity ->
                ActivityUI(
                    activity = activity,
                )
            },
        )
    )
}
