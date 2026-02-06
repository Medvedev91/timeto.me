package me.timeto.shared

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

object TimeFlows {

    val eachMinuteSecondsFlow = MutableStateFlow(calcLastMinuteTime())

    suspend fun launchFlows() {
        eachMinuteSecondsFlow.emit(calcLastMinuteTime())
        while (true) {
            val now = time()
            val secondsToNextMinute: Int = 60 - (now % 60)
            delay(secondsToNextMinute * 1_000L)
            eachMinuteSecondsFlow.emit(now + secondsToNextMinute)
        }
    }

    ///

    private fun calcLastMinuteTime(): Int {
        val now = time()
        val rem = now % 60
        return now - rem
    }
}
