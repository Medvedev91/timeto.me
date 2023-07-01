package me.timeto.shared.vm.ui

import me.timeto.shared.BREAK_SECONDS
import me.timeto.shared.ColorNative
import me.timeto.shared.db.IntervalModel
import me.timeto.shared.time
import me.timeto.shared.toHms
import kotlin.math.absoluteValue

class TimerDataUI(
    interval: IntervalModel,
    isCountdown: Boolean,
    defColor: ColorNative,
) {

    val isCompact: Boolean

    val title: String // 12:34
    val subtitle: String? // NULL / BREAK / OVERDUE
    val color: ColorNative

    init {
        val now = time()
        val timeLeft = interval.id + interval.deadline - now

        // Subtitle?, Subtitle Color, Countdown Time
        val tmpData: Triple<String?, ColorNative, Int> = when {
            timeLeft < -BREAK_SECONDS -> Triple("OVERDUE", ColorNative.red, -timeLeft - BREAK_SECONDS)
            timeLeft <= 0 -> Triple("BREAK", ColorNative.green, timeLeft + BREAK_SECONDS)
            else -> Triple(null, defColor, timeLeft)
        }

        val timeForTitle = if (isCountdown) tmpData.third else (now - interval.id)
        isCompact = timeForTitle >= (3_600 * 10)

        title = secondsToString(timeForTitle)
        subtitle = tmpData.first
        color = if (isCountdown) tmpData.second else ColorNative.purple
    }
}

private fun secondsToString(seconds: Int): String {
    val hms = seconds.absoluteValue.toHms()
    val h = if (hms[0] > 0) "${hms[0]}:" else ""
    val m = hms[1].toString().padStart(2, '0') + ":"
    val s = hms[2].toString().padStart(2, '0')
    return "$h$m$s"
}
