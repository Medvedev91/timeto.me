package me.timeto.shared

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import me.timeto.shared.db.KvDb
import me.timeto.shared.db.KvDb.Companion.asDayStartOffsetSeconds

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

    fun buildTodayWithDayStartOffsetFlow(): Flow<Int> = combine(
        KvDb.KEY.DAY_START_OFFSET_SECONDS.selectOrNullFlow().distinctUntilChanged(),
        eachMinuteSecondsFlow,
    ) { dayStartOffsetSecondsKvDb, nowTime ->
        DayStartOffsetUtils.calcDay(
            time = nowTime,
            dayStartOffsetSeconds = dayStartOffsetSecondsKvDb.asDayStartOffsetSeconds(),
        )
    }.distinctUntilChanged()
}

private fun calcLastMinuteTime(): Int {
    val now = time()
    val rem = now % 60
    return now - rem
}
