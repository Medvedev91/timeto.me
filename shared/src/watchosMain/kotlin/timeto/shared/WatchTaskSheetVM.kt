package timeto.shared

import kotlinx.coroutines.flow.*
import timeto.shared.db.ActivityModel
import timeto.shared.db.TaskModel
import timeto.shared.vm.TaskSheetVM
import timeto.shared.vm.__VM
import timeto.shared.vm.ui.TimerHintUI

class WatchTaskSheetVM(
    val task: TaskModel,
) : __VM<WatchTaskSheetVM.State>() {

    inner class ActivityUI(
        val activity: ActivityModel,
        val historySeconds: List<Int>,
    ) {

        val listTitle = TextFeatures.parse(activity.nameWithEmoji()).textUI()

        val timerHints = TimerHintUI.buildList(
            activity,
            isShort = true,
            historyLimit = 4,
            customLimit = 4,
            primaryHints = historySeconds,
        ) { seconds ->
            WatchToIosSync.startTaskWithLocal(
                activity = activity,
                deadline = seconds,
                task = task
            )
        }
    }

    data class State(
        val activitiesUI: List<ActivityUI>,
    )

    override val state: MutableStateFlow<State>

    init {
        val historySecondsMap = TaskSheetVM.prepHistorySecondsMap(task.text)
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
