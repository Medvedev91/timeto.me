package me.timeto.shared

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetIn
import me.timeto.shared.db.KvDb
import me.timeto.shared.db.KvDb.Companion.asDayStartOffsetSeconds

object DayStartOffsetUtils {

    fun getLocalUtcOffsetCached(): Int =
        localUtcOffset - getOffsetSecondsCached()

    suspend fun getOffsetSeconds(): Int =
        KvDb.KEY.DAY_START_OFFSET_SECONDS.selectOrNull().asDayStartOffsetSeconds()

    fun getOffsetSecondsCached(): Int =
        KvDb.KEY.DAY_START_OFFSET_SECONDS.selectOrNullCached().asDayStartOffsetSeconds()

    suspend fun getDay(): Int = calcDay(
        time = time(),
        dayStartOffsetSeconds = getOffsetSeconds(),
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
