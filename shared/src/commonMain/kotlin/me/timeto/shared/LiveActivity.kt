package me.timeto.shared

import kotlinx.coroutines.flow.MutableSharedFlow
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb

data class LiveActivity(
    val intervalDb: IntervalDb,
) {

    companion object {

        // Not StateFlow to reschedule same data object
        val flow = MutableSharedFlow<LiveActivity?>()
    }

    ///

    val activityDb: ActivityDb =
        intervalDb.selectActivityDbCached()

    val dynamicIslandTitle: String =
        (intervalDb.note ?: activityDb.name).textFeatures().textNoFeatures
}
