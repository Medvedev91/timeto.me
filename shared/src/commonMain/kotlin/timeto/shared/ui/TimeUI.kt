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
            timeLeftText = "In " + secondsToString(secondsLeft)
            color = if (secondsLeft <= 3_600) ColorNative.blue else ColorNative.textSecondary
        } else {
            timeLeftText = secondsOverdueToString(secondsLeft)
            color = ColorNative.red
        }
    }
}

//////

private fun secondsToString(
    secondsAnySign: Int,
): String {
    val (h, m) = secondsAnySign.absoluteValue.toHms()
    if ((h == 0) && (m == 0))
        return "less than a minute"

    val strings = mutableListOf<String>()
    if (h > 0) strings.add(h.toStringEndingHours())
    if (h <= 1 && m > 0) strings.add(m.toStringEndingMinutes())
    val separator = if (m <= 5) " and " else " "
    return strings.joinToString(separator)
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
private fun Int.toStringEndingMinutes() = toStringEnding(true, "minute", "minutes")
