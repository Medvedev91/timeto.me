package me.timeto.shared

import kotlin.time.Clock

fun timeMls(): Long =
    Clock.System.now().toEpochMilliseconds()
