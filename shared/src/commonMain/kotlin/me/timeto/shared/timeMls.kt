package me.timeto.shared

import kotlinx.datetime.Clock

fun timeMls(): Long =
    Clock.System.now().toEpochMilliseconds()
