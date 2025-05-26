package me.timeto.app.misc.extensions

import me.timeto.shared.UnixTime
import java.util.Date

fun Date.toUnixTime() =
    UnixTime((this.time / 1_000L).toInt())
