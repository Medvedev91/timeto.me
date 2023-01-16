package timeto.shared.vm.ui

import timeto.shared.*
import kotlin.math.absoluteValue

class DaytimeUI(
    val daytime: Int,
) {

    val daytimeText = TextFeatures.daytimeToString(daytime)
    val color: ColorNative
    val timeLeftText: String

    init {
        val dayStartTime = UnixTime().localDayStartTime()
        val secondsSinceDayStart = time() - dayStartTime
        val secondsLeft = daytime - secondsSinceDayStart
        if (secondsLeft > 0) {
            color = if (secondsLeft <= 3_600) ColorNative.blue else ColorNative.textSecondary
            timeLeftText = "In " + secondsToString(secondsLeft, isOverdueOrIn = false)
        } else {
            color = ColorNative.red
            timeLeftText = secondsToString(secondsLeft, isOverdueOrIn = true) + " overdue"
        }
    }
}

private fun secondsToString(
    secondsAnySign: Int,
    isOverdueOrIn: Boolean,
): String {
    val (h, m) = secondsAnySign.absoluteValue.toHms()

    if (isOverdueOrIn) {
        if (h == 0)
            return if (m == 0) "~1 minute"
            else m.toStringEndingMinutes()
        return h.toStringEndingHours()
    }

    if ((h == 0) && (m == 0))
        return "less than a minute"

    val strings = mutableListOf<String>()
    if (h > 0) strings.add(h.toStringEndingHours())
    if (h <= 1 && m > 0) strings.add(m.toStringEndingMinutes())
    val separator = if (m <= 5) " and " else " "
    return strings.joinToString(separator)
}

private fun Int.toStringEndingHours() = toStringEnding(true, "hour", "hours")
private fun Int.toStringEndingMinutes() = toStringEnding(true, "minute", "minutes")
