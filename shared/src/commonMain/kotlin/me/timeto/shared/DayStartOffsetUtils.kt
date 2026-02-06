package me.timeto.shared

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetIn
import me.timeto.shared.db.KvDb
import me.timeto.shared.db.KvDb.Companion.asDayStartOffsetSeconds

object DayStartOffsetUtils {

    suspend fun getDay(): Int = calcDay(
        time = time(),
        dayStartOffsetSeconds =
            KvDb.KEY.DAY_START_OFFSET_SECONDS.selectOrNull().asDayStartOffsetSeconds(),
    )

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
