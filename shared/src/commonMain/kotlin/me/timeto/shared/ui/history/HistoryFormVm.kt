package me.timeto.shared.ui.history

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.Cache
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.__Vm

class HistoryFormVm(
    initIntervalDb: IntervalDb?,
) : __Vm<HistoryFormVm.State>() {

    data class State(
        val initIntervalDb: IntervalDb?,
        val initActivityDb: ActivityDb,
        val activitiesUi: List<ActivityUi>,
    ) {

        val title: String =
            if (initIntervalDb == null) "New Entry" else "Edit"
        val saveText: String =
            if (initIntervalDb == null) "Create" else "Save"

        val activityTitle = "Activity"
        val timeTitle = "Time Start"
    }

    override val state = MutableStateFlow(
        State(
            initIntervalDb = initIntervalDb,
            initActivityDb =
                initIntervalDb?.selectActivityDbCached() ?: ActivityDb.selectOtherCached(),
            activitiesUi =
                Cache.activitiesDbSorted.map { ActivityUi(activityDb = it) },
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
