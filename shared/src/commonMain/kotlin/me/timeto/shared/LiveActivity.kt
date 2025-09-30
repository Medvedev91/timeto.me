package me.timeto.shared

import kotlinx.coroutines.flow.MutableSharedFlow
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.db.IntervalDb

data class LiveActivity(
    val intervalDb: IntervalDb,
) {

    companion object {

        // Not StateFlow to reschedule same data object
        val flow = MutableSharedFlow<LiveActivity?>()
    }

    ///

    val goalDb: Goal2Db =
        intervalDb.selectGoalDbCached()

    val dynamicIslandTitle: String =
        (intervalDb.note ?: goalDb.name).textFeatures().textNoFeatures
}
