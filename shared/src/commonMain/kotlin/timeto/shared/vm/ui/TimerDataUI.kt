package timeto.shared.vm.ui

import timeto.shared.BREAK_SECONDS
import timeto.shared.ColorNative
import timeto.shared.db.IntervalModel
import timeto.shared.time
import timeto.shared.toHms
import kotlin.math.absoluteValue

class TimerDataUI(
    interval: IntervalModel,
    defColor: ColorNative,
) {

    companion object {

        fun secondsToString(seconds: Int): String {
            val hms = seconds.absoluteValue.toHms()
            val h = if (hms[0] > 0) "${hms[0]}:" else ""
            val m = hms[1].toString().padStart(2, '0') + ":"
            val s = hms[2].toString().padStart(2, '0')
            return "$h$m$s"
        }
    }

    val timer: String
    val subtitle: String? // NULL / BREAK / OVERDUE
    val timePassedNote = secondsToString(time() - interval.id) // Time left
    val color: ColorNative
    val isCompact: Boolean

    init {
        val timeLeft = interval.id + interval.deadline - time()
        val timeForTimer: Int

        when {
            timeLeft < -BREAK_SECONDS -> {
                timeForTimer = -timeLeft - BREAK_SECONDS
                color = ColorNative.red
                subtitle = "OVERDUE"
            }
            timeLeft <= 0 -> {
                timeForTimer = timeLeft + BREAK_SECONDS
                color = ColorNative.green
                subtitle = "BREAK"
            }
            else -> {
                timeForTimer = timeLeft
                color = defColor
                subtitle = null
            }
        }

        timer = secondsToString(timeForTimer)
        isCompact = timeForTimer >= (3_600 * 10)
    }
}
