package me.timeto.shared

import kotlinx.datetime.*

fun time(): Int =
    Clock.System.now().epochSeconds.toInt()

fun timeMls(): Long =
    Clock.System.now().toEpochMilliseconds()
