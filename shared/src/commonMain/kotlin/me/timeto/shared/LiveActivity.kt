package me.timeto.shared

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb

data class LiveActivity(
    val intervalDb: IntervalDb,
    val enabled: Boolean,
) {

    val activityDb: ActivityDb =
        intervalDb.selectActivityDbCached()

    val dynamicIslandTitle: String =
        (intervalDb.note ?: activityDb.name).textFeatures().textNoFeatures

    ///

    companion object {

        val flow = MutableStateFlow<LiveActivity?>(null)

        suspend fun update(
            intervalDb: IntervalDb,
            enabled: Boolean,
        ) {
            flow.emit(
                LiveActivity(
                    intervalDb = intervalDb,
                    enabled = enabled,
                )
            )
        }
    }
}
