package timeto.shared

import kotlinx.coroutines.flow.*
import timeto.shared.db.ActivityModel
import timeto.shared.db.IntervalModel
import timeto.shared.ui.IntervalNoteUI
import timeto.shared.vm.__VM
import timeto.shared.ui.TimerHintUI

class WatchTabTimerVM : __VM<WatchTabTimerVM.State>() {

    class ActivityUI(
        val activity: ActivityModel,
        val noteUI: IntervalNoteUI?,
        val isActive: Boolean,
    ) {

        val listTitle = TextFeatures.parse(activity.nameWithEmoji()).textUi

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
    activitiesUI = activities.toUiList(lastInterval)
)

private fun List<ActivityModel>.toUiList(
    lastInterval: IntervalModel
): List<WatchTabTimerVM.ActivityUI> {
    // On top the active activity :)
    val sorted = this.sortedByDescending { it.id == lastInterval.activity_id }
    val activeIdx = sorted.indexOfFirst { it.id == lastInterval.activity_id }
    return sorted.mapIndexed { idx, activity ->
        val isActive = (idx == activeIdx)
        val noteUI = if (isActive && lastInterval.note != null)
            IntervalNoteUI(lastInterval.note, checkLeadingEmoji = false)
        else null
        WatchTabTimerVM.ActivityUI(
            activity = activity,
            noteUI = noteUI,
            isActive = isActive,
        )
    }
}
