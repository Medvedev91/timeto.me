package timeto.shared

import kotlinx.coroutines.flow.*
import timeto.shared.db.ActivityModel
import timeto.shared.db.IntervalModel
import timeto.shared.vm.__VM
import timeto.shared.vm.ui.TimerHintUI

class WatchTabTimerVM : __VM<WatchTabTimerVM.State>() {

    class ActivityUI(
        val activity: ActivityModel,
    ) {

        val listTitle = TextFeatures.parse(activity.nameWithEmoji()).textNoFeatures

        val timerHints = TimerHintUI.buildList(
            activity,
            isShort = true,
            historyLimit = 4,
            customLimit = 4,
        ) { seconds ->
            WatchToIosSync.startIntervalWithLocal(
                activity = activity,
                deadline = seconds,
            )
        }
    }

    data class State(
        val lastInterval: IntervalModel,
        val activitiesUI: List<ActivityUI>,
    )

    override val state = MutableStateFlow(
        prepState(DI.lastInterval, DI.activitiesSorted)
    )

    override fun onAppear() {
        val scope = scopeVM()
        IntervalModel.getLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) { lastInterval ->
                state.update {
                    prepState(lastInterval, ActivityModel.getAscSorted())
                }
            }
        // In case if hints would change
        ActivityModel.getAscSortedFlow()
            .onEachExIn(scope) { activities ->
                state.update {
                    prepState(IntervalModel.getLastOneOrNull()!!, activities)
                }
            }
    }
}

private fun prepState(
    lastInterval: IntervalModel,
    activities: List<ActivityModel>,
) = WatchTabTimerVM.State(
    lastInterval = lastInterval,
    activitiesUI = activities
        // On top the active activity :)
        .sortedByDescending { it.id == lastInterval.activity_id }
        .map { activity ->
            WatchTabTimerVM.ActivityUI(
                activity = activity
            )
        }
)
