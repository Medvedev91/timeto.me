package me.timeto.shared

import kotlinx.datetime.Clock

fun time(): Int =
    Clock.System.now().epochSeconds.toInt()
