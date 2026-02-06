package me.timeto.shared

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

object TimeFlows {

    val todayFlow = MutableStateFlow(UnixTime().localDay)
    val eachMinuteSecondsFlow = MutableStateFlow(calcLastMinuteTime())

    suspend fun launchFlows() {
        eachMinuteSecondsFlow.emit(calcLastMinuteTime())
        while (true) {
            val now = time()
            val secondsToNextMinute: Int = 60 - (now % 60)
            delay(secondsToNextMinute * 1_000L)
            val nextMinuteTime: Int = now + secondsToNextMinute
            todayFlow.emit(UnixTime(time = nextMinuteTime).localDay)
            eachMinuteSecondsFlow.emit(nextMinuteTime)
        }
    }
}

private fun calcLastMinuteTime(): Int {
    val now = time()
    val rem = now % 60
    return now - rem
}
