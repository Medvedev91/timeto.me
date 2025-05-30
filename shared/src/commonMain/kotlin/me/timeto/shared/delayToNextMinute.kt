package me.timeto.shared

import kotlinx.coroutines.delay
import me.timeto.shared.misc.time

internal suspend fun delayToNextMinute(extraMls: Long = 1_000L) {
    val secondsToNewMinute = 60 - (time() % 60)
    delay((secondsToNewMinute * 1_000L) + extraMls)
}
