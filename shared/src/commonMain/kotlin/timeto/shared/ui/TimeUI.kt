package timeto.shared.ui

import timeto.shared.*
import kotlin.math.absoluteValue

class TimeUI(
    val unixTime: UnixTime
) {

    val daytimeText = daytimeToString(unixTime.time - unixTime.localDayStartTime())
    val timeLeftText: String
    val color: ColorNative

    init {
        val secondsLeft = unixTime.time - time()
        if (secondsLeft > 0) {
            timeLeftText = secondsInToString(secondsLeft)
            color = if (secondsLeft <= 3_600) ColorNative.blue else ColorNative.textSecondary
        } else {
            timeLeftText = secondsOverdueToString(secondsLeft)
            color = ColorNative.red
        }
    }
}

//////

private fun secondsInToString(seconds: Int): String {
    // With roundToNextMinute it's impossible to (h == 0 && m == 0)
    val (h, m) = seconds.toHms(roundToNextMinute = true)
    return if (h > 0) "In ${h.toStringEndingHours()}" else "In $m min"
}

private fun secondsOverdueToString(seconds: Int): String {
    val (h, m) = seconds.absoluteValue.toHms()
    return when {
        h > 0 -> h.toStringEndingHours() + " overdue"
        m == 0 -> "Now! 🙀"
        else -> "$m min overdue"
    }
}

private fun Int.toStringEndingHours() = toStringEnding(true, "hour", "hours")
