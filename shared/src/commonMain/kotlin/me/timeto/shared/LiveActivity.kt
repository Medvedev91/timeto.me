package me.timeto.shared

import kotlinx.coroutines.flow.MutableSharedFlow
import me.timeto.shared.db.IntervalDb

data class LiveActivity(
    val intervalDb: IntervalDb,
) {

    companion object {

        // Not StateFlow to reschedule same data object
        val flow = MutableSharedFlow<LiveActivity?>()
    }

    ///

    val timerType: IntervalDb.TimerType =
        intervalDb.buildTimerType()

    val dynamicIslandTitle: String =
        intervalDb.noteOrActivityName()
}
