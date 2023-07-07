package me.timeto.shared.vm.ui

import me.timeto.shared.*
import me.timeto.shared.db.IntervalModel
import kotlin.math.absoluteValue

class TimerDataUI(
    interval: IntervalModel,
    isCountdown: Boolean,
    defColor: ColorNative,
) {

    val isCompact: Boolean
    val status: STATUS
    val title: String // 12:34
    val subtitle: String? // NULL / BREAK / OVERDUE
    val color: ColorNative
    val restartText = interval.deadline.toTimerHintNote(isShort = true)

    init {
        val now = time()
        val timeLeft = interval.id + interval.deadline - now

        class TmpDTO(val subtitle: String?, val color: ColorNative, val timeLeft: Int, val status: STATUS)

        val tmpData: TmpDTO = when {
            timeLeft < -BREAK_SECONDS -> TmpDTO("OVERDUE", ColorNative.red, -timeLeft - BREAK_SECONDS, STATUS.OVERDUE)
            timeLeft <= 0 -> TmpDTO("BREAK", ColorNative.green, timeLeft + BREAK_SECONDS, STATUS.BREAK)
            else -> TmpDTO(null, defColor, timeLeft, STATUS.WORK)
        }

        val timeForTitle = if (isCountdown) tmpData.timeLeft else (now - interval.id)
        isCompact = timeForTitle >= (3_600 * 10)

        status = tmpData.status
        title = secondsToString(timeForTitle)
        subtitle = tmpData.subtitle
        color = if (isCountdown) tmpData.color else ColorNative.purple
    }

    enum class STATUS {

        WORK, BREAK, OVERDUE;

        fun isWork() = this == WORK
        fun isBreak() = this == BREAK
        fun isOverdue() = this == OVERDUE
    }
}

private fun secondsToString(seconds: Int): String {
    val hms = seconds.absoluteValue.toHms()
    val h = if (hms[0] > 0) "${hms[0]}:" else ""
    val m = hms[1].toString().padStart(2, '0') + ":"
    val s = hms[2].toString().padStart(2, '0')
    return "$h$m$s"
}
