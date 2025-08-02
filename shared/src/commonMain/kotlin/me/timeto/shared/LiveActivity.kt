package me.timeto.shared

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb

data class LiveActivity(
    val intervalDb: IntervalDb,
    val activityDb: ActivityDb,
    val enabled: Boolean,
) {

    val dynamicIslandTitle: String =
        (intervalDb.note ?: activityDb.name).textFeatures().textNoFeatures

    ///

    companion object {

        val flow = MutableStateFlow<LiveActivity?>(null)

        suspend fun update(
            intervalDb: IntervalDb,
            enabled: Boolean,
        ) {
            val activityDb = intervalDb.selectActivityDb()
            flow.emit(
                LiveActivity(
                    intervalDb = intervalDb,
                    activityDb = activityDb,
                    enabled = enabled,
                )
            )
        }
    }
}
