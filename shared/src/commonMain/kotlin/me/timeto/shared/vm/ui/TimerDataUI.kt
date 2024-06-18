package me.timeto.shared.vm.ui

import me.timeto.shared.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.TaskDb
import kotlin.math.absoluteValue

class TimerDataUI(
    interval: IntervalDb,
    todayTasks: List<TaskDb>,
    isPurple: Boolean,
) {

    val status: STATUS

    val note: String
    val noteColor: ColorRgba

    val timerText: String
    val timerColor: ColorRgba

    ///

    private val activity: ActivityDb = interval.getActivityDI()
    private val pausedTaskData: PausedTaskData? = run {
        if (!activity.isOther())
            return@run null
        val note: String = interval.note
            ?: return@run null
        val pausedTaskId: Int = note.textFeatures().pause?.pausedTaskId
            ?: return@run null
        val pausedTask: TaskDb = todayTasks.firstOrNull { it.id == pausedTaskId }
            ?: return@run null
        val pausedTaskTf: TextFeatures = pausedTask.text.textFeatures()
        val pausedTaskTimer: Int = pausedTaskTf.paused?.timer
            ?: return@run null
        val pausedActivityDb: ActivityDb = pausedTaskTf.activity
            ?: return@run null
        PausedTaskData(
            taskDb = pausedTask,
            activityDb = pausedActivityDb,
            timer = pausedTaskTimer,
            note = "Break - " + pausedTaskTf.textUi(
                withActivityEmoji = false,
                withTimer = false,
            ),
        )
    }

    private val restartTimer = interval.note?.textFeatures()?.paused?.timer ?: interval.timer
    val restartText = restartTimer.toTimerHintNote(isShort = true)

    init {
        note = pausedTaskData?.note ?: run {
            val tf = (interval.note ?: activity.name)
            tf.textFeatures().textUi(
                withActivityEmoji = false,
                withTimer = false,
            )
        }

        val now = time()
        val timeLeft = interval.id + interval.timer - now

        class TmpDTO(val color: ColorRgba, val timeLeft: Int, val status: STATUS)

        val pomodoro: Int = interval.getActivityDI().pomodoro_timer
        val tmpData: TmpDTO = when {
            timeLeft < -pomodoro -> TmpDTO(ColorRgba.red, -timeLeft - pomodoro, STATUS.OVERDUE)
            timeLeft <= 0 -> TmpDTO(ColorRgba.green, timeLeft + pomodoro, STATUS.BREAK)
            else -> TmpDTO(defColor, timeLeft, STATUS.PROCESS)
        }

        val timeForTitle = if (isPurple) (now - interval.id) else tmpData.timeLeft

        status = tmpData.status
        title = secondsToString(timeForTitle)
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

private data class PausedTaskData(
    val taskDb: TaskDb,
    val activityDb: ActivityDb,
    val timer: Int,
    val note: String,
)
