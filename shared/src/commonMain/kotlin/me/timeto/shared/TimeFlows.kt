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
            // If the application goes into the background for a
            // long period of time during the delay, the real
            // time must be recalculated after return foreground.
            val predictedTime: Int = now + secondsToNextMinute
            // limitMin() fix if time() previous minute like 1ms ago.
            val realTime: Int = calcLastMinuteTime().limitMin(predictedTime)
            todayFlow.tryEmit(UnixTime(time = realTime).localDay)
            eachMinuteSecondsFlow.tryEmit(realTime)
        }
    }
}

private fun calcLastMinuteTime(): Int {
    val now = time()
    val rem = now % 60
    return now - rem
}
