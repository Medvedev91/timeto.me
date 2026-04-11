package me.timeto.shared

import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetIn
import kotlin.time.Clock

var localUtcOffset: Int =
    calcLocalUtcOffset()

fun localUtcOffsetSync() {
    localUtcOffset = calcLocalUtcOffset()
}

private fun calcLocalUtcOffset(): Int =
    Clock.System.now().offsetIn(TimeZone.currentSystemDefault()).totalSeconds
