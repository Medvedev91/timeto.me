package me.timeto.shared.misc

import kotlinx.datetime.*

fun time(): Int =
    Clock.System.now().epochSeconds.toInt()

fun timeMls(): Long =
    Clock.System.now().toEpochMilliseconds()
