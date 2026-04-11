package me.timeto.shared

import kotlin.time.Clock

fun time(): Int =
    Clock.System.now().epochSeconds.toInt()
