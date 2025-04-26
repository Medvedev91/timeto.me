package me.timeto.shared.ui.activities

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.Cache
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.__Vm

class ActivityPickerVm : __Vm<ActivityPickerVm.State>() {

    data class State(
        val activitiesUi: List<ActivityUi>,
    )

    override val state = MutableStateFlow(
        State(
            activitiesUi = Cache.activitiesDbSorted.map { ActivityUi(it) },
        )
    )

    ///

    data class ActivityUi(
        val activityDb: ActivityDb,
    ) {
        val title: String =
            activityDb.name.textFeatures().textNoFeatures
    }
}
