package me.timeto.shared.ui.activities

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.launchExIo
import me.timeto.shared.onEachExIn
import me.timeto.shared.textFeatures
import me.timeto.shared.ui.misc.moveAndroid
import me.timeto.shared.ui.misc.moveIos
import me.timeto.shared.vm.__Vm

class ActivitiesFormVm : __Vm<ActivitiesFormVm.State>() {

    data class State(
        val activitiesDb: List<ActivityDb>,
    ) {

        val title = "Activities"

        val activitiesUi: List<ActivityUi> =
            activitiesDb.map { ActivityUi(it) }
    }

    override val state = MutableStateFlow(
        State(
            activitiesDb = Cache.activitiesDbSorted,
        )
    )

    init {
        val scopeVm = scopeVm()
        ActivityDb.selectSortedFlow().onEachExIn(scopeVm) { activitiesDb ->
            state.update { it.copy(activitiesDb = activitiesDb) }
        }
    }

    ///

    fun moveAndroidLocal(fromIdx: Int, toIdx: Int) {
        state.value.activitiesDb.moveAndroid(fromIdx, toIdx) { newItems ->
            state.update { it.copy(activitiesDb = newItems) }
        }
    }

    fun moveAndroidSync() {
        launchExIo {
            ActivityDb.updateSortMany(state.value.activitiesDb)
        }
    }

    fun moveIos(fromIdx: Int, toIdx: Int) {
        state.value.activitiesDb.moveIos(fromIdx, toIdx) { newActivitiesDb ->
            ActivityDb.updateSortMany(newActivitiesDb)
        }
    }

    ///

    data class ActivityUi(
        val activityDb: ActivityDb,
    ) {
        val title: String =
            activityDb.name.textFeatures().textNoFeatures
    }
}
