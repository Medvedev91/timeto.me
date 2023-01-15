package timeto.shared.vm.ui

import timeto.shared.*
import kotlin.math.absoluteValue

class DaytimeUI(
    daytime: Int,
) {
    val daytimeText = TextFeatures.daytimeToString(daytime)
    val color: ColorNative = ColorNative.blue
}

private fun secondsToString(
    secondsAnySign: Int,
    isShort: Boolean,
): String {
    val (h, m) = secondsAnySign.absoluteValue.toHms()

    if (isShort) {
        if (h == 0)
            return if (m == 0) "~1 minute"
            else m.toStringEndingMinutes()
        if (h <= 2)
            return if (m == 0) h.toStringEndingHours()
            else "${h.toStringEndingHours()} $m min"
        return h.toStringEndingHours()
    }

    if ((h == 0) && (m == 0))
        return "less than a minute"

    val strings = mutableListOf<String>()
    if (h > 0) strings.add(h.toStringEndingHours())
    if (h <= 2 && m > 0) strings.add(m.toStringEndingMinutes())
    val separator = if (m <= 5) " and " else " "
    return strings.joinToString(separator)
}

private fun Int.toStringEndingHours() = toStringEnding(true, "hour", "hours")
private fun Int.toStringEndingMinutes() = toStringEnding(true, "minute", "minutes")
