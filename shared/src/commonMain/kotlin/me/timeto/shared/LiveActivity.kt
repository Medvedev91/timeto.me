package me.timeto.shared

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb

data class LiveActivity(
    val intervalDb: IntervalDb,
) {

    companion object {

        val flow = MutableStateFlow<LiveActivity?>(null)
    }

    ///

    val activityDb: ActivityDb =
        intervalDb.selectActivityDbCached()

    val dynamicIslandTitle: String =
        (intervalDb.note ?: activityDb.name).textFeatures().textNoFeatures
}
