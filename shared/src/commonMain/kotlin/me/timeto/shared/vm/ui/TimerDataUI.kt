package me.timeto.shared.vm.ui

import me.timeto.shared.*
import me.timeto.shared.db.IntervalDb
import kotlin.math.absoluteValue

class TimerDataUI(
    interval: IntervalDb,
    isPurple: Boolean,
    defColor: ColorRgba,
) {

    val status: STATUS
    val title: String // 12:34
    val subtitle: String? // NULL / BREAK / OVERDUE
    val color: ColorRgba

    private val restartTimer = interval.note?.textFeatures()?.paused?.timer ?: interval.timer
    val restartText = restartTimer.toTimerHintNote(isShort = true)

    init {
        val now = time()
        val timeLeft = interval.id + interval.timer - now

        class TmpDTO(val subtitle: String?, val color: ColorRgba, val timeLeft: Int, val status: STATUS)

        val tmpData: TmpDTO = when {
            timeLeft < -BREAK_SECONDS -> TmpDTO("OVERDUE", ColorRgba.red, -timeLeft - BREAK_SECONDS, STATUS.OVERDUE)
            timeLeft <= 0 -> TmpDTO("BREAK", ColorRgba.green, timeLeft + BREAK_SECONDS, STATUS.BREAK)
            else -> TmpDTO(null, defColor, timeLeft, STATUS.PROCESS)
        }

        val timeForTitle = if (isPurple) (now - interval.id) else tmpData.timeLeft

        status = tmpData.status
        title = secondsToString(timeForTitle)
        subtitle = tmpData.subtitle
        color = if (isPurple) ColorRgba.purple else tmpData.color
    }

    fun restart() {
        launchExDefault {
            val lastInterval = IntervalDb.getLastOneOrNull()!!
            IntervalDb.addWithValidation(restartTimer, lastInterval.getActivityDI(), lastInterval.note)
        }
    }

    enum class STATUS {

        PROCESS, BREAK, OVERDUE;

        fun isProcess() = this == PROCESS
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
