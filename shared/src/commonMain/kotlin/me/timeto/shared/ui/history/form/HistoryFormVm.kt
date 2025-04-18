package me.timeto.shared.ui.history.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.misc.time
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.__Vm

class HistoryFormVm(
    initIntervalDb: IntervalDb?,
) : __Vm<HistoryFormVm.State>() {

    data class State(
        val initIntervalDb: IntervalDb?,
        val activityDb: ActivityDb,
        val time: Int,
        val activitiesUi: List<ActivityUi>,
    ) {

        val title: String =
            if (initIntervalDb == null) "New Entry" else "Edit"
        val saveText: String =
            if (initIntervalDb == null) "Create" else "Save"

        val activityTitle = "Activity"
        val activityNote: String =
            activityDb.name.textFeatures().textNoFeatures

        val timeTitle = "Time Start"
        val timeNote: String =
            HistoryFormUtils.prepTimeNote(time)
    }

    override val state = MutableStateFlow(
        State(
            initIntervalDb = initIntervalDb,
            activityDb = initIntervalDb?.selectActivityDbCached()
                         ?: ActivityDb.selectOtherCached(),
            time = initIntervalDb?.id ?: time(),
            activitiesUi = Cache.activitiesDbSorted.map { ActivityUi(activityDb = it) },
        )
    )

    ///

    fun setActivityDb(newActivityDb: ActivityDb) {
        state.update { it.copy(activityDb = newActivityDb) }
    }

    fun setTime(newTime: Int) {
        state.update { it.copy(time = newTime) }
    }

    ///

    data class ActivityUi(
        val activityDb: ActivityDb,
    ) {
        val title: String =
            activityDb.name.textFeatures().textNoFeatures
    }
}
