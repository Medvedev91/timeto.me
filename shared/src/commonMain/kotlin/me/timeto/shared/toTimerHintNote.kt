package me.timeto.shared

fun Int.toTimerHintNote(
    isShort: Boolean,
): String {
    val (h, m) = this.toHms()
    return when {
        h == 0 -> "${m}${if (isShort) "" else " min"}"
        m == 0 -> "${h}h"
        else -> "${h}:${m.toString().padStart(2, '0')}"
    }
}
