package me.timeto.shared.ui.activities

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.onEachExIn
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.__Vm

class ActivitiesVm : __Vm<ActivitiesVm.State>() {

    data class State(
        val activitiesUi: List<ActivityUi>
    )

    override val state = MutableStateFlow(
        State(
            activitiesUi =
                Cache.activitiesDbSorted.map { ActivityUi(it) },
        )
    )

    init {
        val scopeVm = scopeVm()
        ActivityDb.selectSortedFlow().onEachExIn(scopeVm) { activitiesDb ->
            state.update { state ->
                state.copy(activitiesUi = activitiesDb.map { ActivityUi(it) })
            }
        }
    }

    ///

    class ActivityUi(
        val activityDb: ActivityDb,
    ) {
        val text: String =
            activityDb.name.textFeatures().textUi()
        val isActive: Boolean =
            Cache.lastInterval.activity_id == activityDb.id
    }
}
