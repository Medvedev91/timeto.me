package me.timeto.shared

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.db.TaskModel
import me.timeto.shared.vm.ActivitiesTimerSheetVM
import me.timeto.shared.vm.__VM
import me.timeto.shared.ui.TimerHintUI

class WatchTaskSheetVM(
    val task: TaskModel,
) : __VM<WatchTaskSheetVM.State>() {

    inner class ActivityUI(
        val activity: ActivityModel,
        val historySeconds: List<Int>,
    ) {

        val listTitle = activity.nameWithEmoji().textFeatures().textUi()

        val timerHints = TimerHintUI.buildList(
            activity,
            historyLimit = 4,
            customLimit = 4,
            primaryHints = historySeconds,
        ) { seconds ->
            WatchToIosSync.startTaskWithLocal(
                activity = activity,
                timer = seconds,
                task = task
            )
        }
    }

    data class State(
        val activitiesUI: List<ActivityUI>,
    )

    override val state: MutableStateFlow<State>

    init {
        val historySecondsMap = ActivitiesTimerSheetVM.prepHistorySecondsMap(task.text)
        // On the top activities with history
        val activitiesSorted = DI.activitiesSorted.sortedByDescending {
            historySecondsMap[it.id] != null
        }
        state = MutableStateFlow(
            State(
                activitiesUI = activitiesSorted.map { activity ->
                    ActivityUI(
                        activity = activity,
                        historySeconds = historySecondsMap[activity.id] ?: listOf(),
                    )
                },
            )
        )
    }
}
