package me.timeto.shared.vm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.timeto.shared.*
import me.timeto.shared.data.TimerTabActivityData
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.db.IntervalModel
import me.timeto.shared.ui.TimerHintUI

class ActivitiesListVM : __VM<ActivitiesListVM.State>() {

    class ActivityUI(
        val activity: ActivityModel,
        val lastInterval: IntervalModel,
        val withTopDivider: Boolean,
    ) {

        val data = TimerTabActivityData(activity, lastInterval, false)

        val timerHints = TimerHintUI.buildList(
            activity,
            isShort = true,
            historyLimit = 2,
            customLimit = 5,
        ) { seconds ->
            activity.startInterval(seconds)
        }
    }

    data class State(
        val activities: List<ActivityModel>,
        val lastInterval: IntervalModel,
        val idToUpdate: Int = 0,
    ) {
        val activitiesUI = activities.toUiList(lastInterval)
    }

    override val state = MutableStateFlow(
        State(
            activities = DI.activitiesSorted,
            lastInterval = DI.lastInterval,
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        ActivityModel.getAscSortedFlow()
            .onEachExIn(scope) { activities ->
                state.update { it.copy(activities = activities) }
            }
        IntervalModel.getLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) { interval ->
                state.update { it.copy(lastInterval = interval) }
            }
        scope.launch {
            while (true) {
                delay(1_000L)
                state.update {
                    it.copy(idToUpdate = it.idToUpdate + 1)
                }
            }
        }
    }
}

private fun List<ActivityModel>.toUiList(
    lastInterval: IntervalModel,
): List<ActivitiesListVM.ActivityUI> {
    val sorted = this.sortedWith(compareBy({ it.sort }, { -it.id }))
    val activeIdx = sorted.indexOfFirst { it.id == lastInterval.activity_id }
    return sorted.mapIndexed { idx, activity ->
        val isActive = (idx == activeIdx)
        ActivitiesListVM.ActivityUI(
            activity = activity,
            lastInterval = lastInterval,
            withTopDivider = (idx != 0) && !isActive && (activeIdx != idx - 1),
        )
    }
}
