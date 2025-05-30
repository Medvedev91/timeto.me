package me.timeto.shared

import kotlinx.datetime.*
import me.timeto.shared.db.KvDb
import me.timeto.shared.db.KvDb.Companion.asDayStartOffsetSeconds

fun time(): Int =
    Clock.System.now().epochSeconds.toInt()

fun timeMls(): Long =
    Clock.System.now().toEpochMilliseconds()

/**
 * todo
 * Store as a constant and update for performance. Now 3+
 * functions are being called and objects are being created.
 */
val localUtcOffset: Int
    get() = Clock.System.now().offsetIn(TimeZone.currentSystemDefault()).totalSeconds

val localUtcOffsetWithDayStart: Int
    get() = localUtcOffset - dayStartOffsetSeconds()

fun dayStartOffsetSeconds(): Int =
    KvDb.KEY.DAY_START_OFFSET_SECONDS.selectOrNullCached().asDayStartOffsetSeconds()
