package me.timeto.shared.ui.timer

import me.timeto.shared.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.misc.ColorEnum
import me.timeto.shared.time
import me.timeto.shared.UiException
import me.timeto.shared.ui.daytime.DaytimeUi
import me.timeto.shared.ui.activities.timer.ActivityTimerStrategy
import kotlin.math.absoluteValue

class TimerDataUi(
    intervalDb: IntervalDb,
    todayTasksDb: List<TaskDb>,
    isPurple: Boolean,
) {

    val controlsColorEnum: ColorEnum?

    val note: String
    val noteColor: ColorEnum

    val timerText: String
    val timerColor: ColorEnum

    val infoUi = InfoUi(intervalDb)
    val prolongText: String?

    ///

    private val intervalNoteTf: TextFeatures? =
        intervalDb.note?.textFeatures()

    private val activityDb: ActivityDb =
        intervalDb.selectActivityDbCached()

    private val pausedTaskData: PausedTaskData? = run {
        if (!activityDb.isOther())
            return@run null
        val pausedTaskId: Int =
            intervalNoteTf?.pause?.pausedTaskId ?: return@run null
        val pausedTask: TaskDb =
            todayTasksDb.firstOrNull { it.id == pausedTaskId } ?: return@run null
        val pausedTaskTf: TextFeatures =
            pausedTask.text.textFeatures()
        val pausedTaskTimer: Int =
            pausedTaskTf.paused?.originalTimer ?: return@run null
        val pausedActivityDb: ActivityDb =
            pausedTaskTf.activity ?: return@run null
        PausedTaskData(
            taskDb = pausedTask,
            taskTextTf = pausedTaskTf,
            activityDb = pausedActivityDb,
            timer = pausedTaskTimer,
        )
    }

    init {

        val now: Int = time()
        val secondsToEnd: Int = intervalDb.id + intervalDb.timer - now

        timerText = secondsToString(if (isPurple) (now - intervalDb.id) else secondsToEnd)
        timerColor = when {
            isPurple -> ColorEnum.purple
            secondsToEnd < 0 -> ColorEnum.red
            pausedTaskData != null -> ColorEnum.green
            else -> ColorEnum.white
        }

        prolongText = run {
            val prolonged: TextFeatures.Prolonged =
                intervalNoteTf?.prolonged ?: return@run null
            (intervalDb.timer - prolonged.originalTimer).toTimerHintNote(true)
        }

        note = (intervalNoteTf ?: activityDb.name.textFeatures()).textUi(
            withActivityEmoji = false,
            withTimer = false,
        )
        noteColor = if (pausedTaskData != null) ColorEnum.green else timerColor

        controlsColorEnum = when {
            isPurple -> ColorEnum.purple
            secondsToEnd < 0 -> ColorEnum.red
            pausedTaskData != null -> ColorEnum.green
            else -> null
        }
    }

    fun togglePomodoro() {
        launchExIo {
            if (pausedTaskData != null) {
                pausedTaskData.taskDb.startInterval(
                    pausedTaskData.timer,
                    pausedTaskData.activityDb,
                )
            } else {
                IntervalDb.pauseLastInterval()
            }
        }
    }

    fun prolong() {
        launchExIo {
            // todo catch exceptions + report
            IntervalDb.prolongLastIntervalEx(5 * 60)
        }
    }

    ///

    class InfoUi(
        private val intervalDb: IntervalDb,
    ) {

        val untilPickerTitle = "Until Time"
        val untilDaytimeUi: DaytimeUi = run {
            val unixTime = UnixTime(intervalDb.id + intervalDb.timer)
            val daytime = unixTime.time - unixTime.localDayStartTime()
            DaytimeUi.byDaytime(daytime)
        }

        val timerText = "Timer"

        val timerStrategy: ActivityTimerStrategy =
            ActivityTimerStrategy.Interval(intervalDb)

        fun setUntilDaytime(daytimeUi: DaytimeUi) {
            val unixTimeNow = UnixTime()
            val dayStartNow = unixTimeNow.localDayStartTime()
            val finishTimeTmp = dayStartNow + daytimeUi.seconds
            // Today / Tomorrow
            val finishTime: Int =
                if (finishTimeTmp > intervalDb.id) finishTimeTmp
                else finishTimeTmp + (3_600 * 24)
            val newTimer = finishTime - intervalDb.id
            launchExIo {
                try {
                    intervalDb.updateTimer(newTimer)
                } catch (e: UiException) {
                    // todo
                    throw e
                }
            }
        }
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
    val taskTextTf: TextFeatures,
    val activityDb: ActivityDb,
    val timer: Int,
)
