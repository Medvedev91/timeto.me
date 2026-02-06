package me.timeto.shared

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetIn

object DayStartOffsetUtils {

    fun calcDay(
        time: Int,
        dayStartOffsetSeconds: Int,
    ): Int {
        val localUtcOffset: Int =
            Clock.System.now().offsetIn(TimeZone.currentSystemDefault()).totalSeconds
        val localUtcOffsetWithDayStart: Int =
            localUtcOffset - dayStartOffsetSeconds
        return UnixTime(
            time = time,
            utcOffset = localUtcOffsetWithDayStart,
        ).localDay
    }
}
