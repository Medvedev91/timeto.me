package me.timeto.shared.ui.history

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.vm.__Vm

class HistoryFormVm(
    initIntervalDb: IntervalDb?,
) : __Vm<HistoryFormVm.State>() {

    data class State(
        val intervalDb: IntervalDb?,
        val activityDb: ActivityDb?,
    ) {

        val title: String =
            if (intervalDb == null) "New Entry" else "Edit"
        val saveText: String =
            if (intervalDb == null) "Create" else "Save"

        val activityTitle = "Activity"
        val timeTitle = "Time Start"
    }

    override val state = MutableStateFlow(
        State(
            intervalDb = initIntervalDb,
            activityDb = initIntervalDb?.selectActivityDbCached()
        )
    )
}
