package me.timeto.shared.ui.activities.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.onEachExIn
import me.timeto.shared.ui.__Vm

class ActivitiesTimerVm : __Vm<ActivitiesTimerVm.State>() {

    data class State(
        val lastIntervalId: Int,
    )

    override val state = MutableStateFlow(
        State(
            lastIntervalId = Cache.lastIntervalDb.id,
        )
    )

    init {
        IntervalDb.selectLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scopeVm()) { newIntervalDb: IntervalDb ->
                state.update { it.copy(lastIntervalId = newIntervalDb.id) }
            }
    }
}
