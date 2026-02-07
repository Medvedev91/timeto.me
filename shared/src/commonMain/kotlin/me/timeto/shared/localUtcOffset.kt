package me.timeto.shared

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetIn

var localUtcOffset: Int =
    calcLocalUtcOffset()

fun localUtcOffsetSync() {
    localUtcOffset = calcLocalUtcOffset()
}

private fun calcLocalUtcOffset(): Int =
    Clock.System.now().offsetIn(TimeZone.currentSystemDefault()).totalSeconds
