package me.timeto.shared.models

import me.timeto.shared.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.vm.ActivityTimerSheetVm
import kotlin.math.absoluteValue

class TimerDataUi(
    interval: IntervalDb,
    todayTasks: List<TaskDb>,
    isPurple: Boolean,
) {

    val controlsColor: ColorRgba

    val note: String
    val noteColor: ColorRgba

    val timerText: String
    val timerColor: ColorRgba

    val infoUi = InfoUi(interval)
    val prolongText: String?

    ///

    private val intervalNoteTf: TextFeatures? = interval.note?.textFeatures()

    private val activity: ActivityDb = interval.getActivityDbCached()
    private val pausedTaskData: PausedTaskData? = run {
        if (!activity.isOther())
            return@run null
        val pausedTaskId: Int = intervalNoteTf?.pause?.pausedTaskId
                                ?: return@run null
        val pausedTask: TaskDb = todayTasks.firstOrNull { it.id == pausedTaskId }
                                 ?: return@run null
        val pausedTaskTf: TextFeatures = pausedTask.text.textFeatures()
        val pausedTaskTimer: Int = pausedTaskTf.paused?.originalTimer
                                   ?: return@run null
        val pausedActivityDb: ActivityDb = pausedTaskTf.activity
                                           ?: return@run null
        PausedTaskData(
            taskDb = pausedTask,
            taskTextTf = pausedTaskTf,
            activityDb = pausedActivityDb,
            timer = pausedTaskTimer,
        )
    }

    init {

        val now = time()
        val secondsToEnd = interval.id + interval.timer - now

        timerText = secondsToString(if (isPurple) (now - interval.id) else secondsToEnd)
        timerColor = when {
            isPurple -> ColorRgba.purple
            secondsToEnd < 0 -> ColorRgba.red
            pausedTaskData != null -> ColorRgba.green
            else -> ColorRgba.white
        }

        prolongText = run {
            val prolonged: TextFeatures.Prolonged = intervalNoteTf?.prolonged
                                                    ?: return@run null
            (interval.timer - prolonged.originalTimer).toTimerHintNote(true)
        }

        note = (intervalNoteTf ?: activity.name.textFeatures()).textUi(
            withActivityEmoji = false,
            withTimer = false,
        )
        noteColor = if (pausedTaskData != null) ColorRgba.green else timerColor

        controlsColor = when {
            isPurple -> ColorRgba.purple
            secondsToEnd < 0 -> ColorRgba.red
            pausedTaskData != null -> ColorRgba.green
            else -> defControlsColor
        }
    }

    fun togglePomodoro() {
        launchExDefault {
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
        launchExDefault {
            IntervalDb.prolongLastInterval(5 * 60)
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
        val timerContext = ActivityTimerSheetVm.TimerContext.Interval(intervalDb)

        fun setUntilDaytime(daytimeUi: DaytimeUi) {
            val unixTimeNow = UnixTime()
            val dayStartNow = unixTimeNow.localDayStartTime()
            val finishTimeTmp = dayStartNow + daytimeUi.seconds
            // Today / Tomorrow
            val finishTime: Int =
                if (finishTimeTmp > intervalDb.id) finishTimeTmp
                else finishTimeTmp + (3_600 * 24)
            val newTimer = finishTime - intervalDb.id
            launchExDefault {
                intervalDb.upTimer(newTimer)
            }
        }
    }
}

private val defControlsColor = ColorRgba(255, 255, 255, 180)

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
