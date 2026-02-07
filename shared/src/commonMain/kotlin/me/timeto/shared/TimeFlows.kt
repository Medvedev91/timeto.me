package me.timeto.shared

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

object TimeFlows {

    val todayFlow = MutableStateFlow(UnixTime().localDay)
    val eachMinuteSecondsFlow = MutableStateFlow(calcLastMinuteTime())

    suspend fun launchFlows() {
        // Using tryEmit to not waiting for subscribers execution,
        // like "emit and forget". tryEmit works well only for StateFlow,
        // for SharedFlow needed to set replay/bufferCapacity.
        todayFlow.tryEmit(UnixTime().localDay)
        eachMinuteSecondsFlow.tryEmit(calcLastMinuteTime())
        while (true) {
            val now = time()
            val secondsToNextMinute: Int = 60 - (now % 60)
            delay(secondsToNextMinute * 1_000L)
            val nextMinuteTime: Int = now + secondsToNextMinute
            todayFlow.tryEmit(UnixTime(time = nextMinuteTime).localDay)
            eachMinuteSecondsFlow.tryEmit(nextMinuteTime)
        }
    }
}

private fun calcLastMinuteTime(): Int {
    val now = time()
    val rem = now % 60
    return now - rem
}
