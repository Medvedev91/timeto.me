package me.timeto.app

import me.timeto.shared.UnixTime
import java.util.Date

fun Date.toUnixTime() =
    UnixTime((this.time / 1_000L).toInt())
