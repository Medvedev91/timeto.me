package me.timeto.shared

import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.TaskDb
import kotlin.math.absoluteValue

class TimerStateUi(
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

    ///

    private val intervalNoteTf: TextFeatures? =
        intervalDb.note?.textFeatures()

    private val activityDb: ActivityDb =
        intervalDb.selectActivityDbCached()

    private val pausedTaskData: PausedTaskData? = run {
        if (!activityDb.isOther)
            return@run null
        val pausedTaskId: Int =
            intervalNoteTf?.pause?.pausedTaskId ?: return@run null
        val pausedTask: TaskDb =
            todayTasksDb.firstOrNull { it.id == pausedTaskId } ?: return@run null
        val pausedTaskTf: TextFeatures =
            pausedTask.text.textFeatures()
        val pausedTaskTimerType: TextFeatures.TimerType =
            pausedTaskTf.paused?.originalTimerType ?: return@run null
        val pausedActivityDb: ActivityDb =
            pausedTaskTf.activityDb ?: return@run null
        PausedTaskData(
            taskDb = pausedTask,
            activityDb = pausedActivityDb,
            timerType = pausedTaskTimerType,
        )
    }

    init {

        val now: Int = time()
        val timerType = intervalDb.buildTimerType()
        val isTimerAndFinished: Boolean =
            (timerType is IntervalDb.TimerType.Timer) && timerType.isFinished(now)
        val isStopwatch: Boolean =
            timerType is IntervalDb.TimerType.Stopwatch

        timerText = secondsToString(
            when {
                isPurple -> now - intervalDb.id
                else -> when (timerType) {
                    is IntervalDb.TimerType.Timer -> timerType.calcRemainingSeconds(now)
                    is IntervalDb.TimerType.Stopwatch -> timerType.calcElapsedSeconds(now)
                }
            }
        )
        timerColor = when {
            isPurple -> ColorEnum.purple
            isStopwatch -> ColorEnum.white
            isTimerAndFinished -> ColorEnum.red
            pausedTaskData != null -> ColorEnum.green
            else -> ColorEnum.white
        }

        note = intervalDb.noteOrActivityName()
        noteColor = if (pausedTaskData != null) ColorEnum.green else timerColor

        controlsColorEnum = when {
            isPurple -> ColorEnum.purple
            isStopwatch -> ColorEnum.white
            isTimerAndFinished -> ColorEnum.red
            pausedTaskData != null -> ColorEnum.green
            else -> null
        }
    }

    fun togglePomodoro() {
        launchExIo {
            // If Break
            if (pausedTaskData != null) {
                val tfTimerType: TextFeatures.TimerType =
                    when (val pausedTimerType = pausedTaskData.timerType) {
                        is TextFeatures.TimerType.Timer -> {
                            val pausedActivityDb: ActivityDb =
                                pausedTaskData.activityDb
                            when (val timerType = pausedActivityDb.buildTimerType()) {
                                ActivityDb.TimerType.TimerPicker,
                                is ActivityDb.TimerType.FixedTimer,
                                is ActivityDb.TimerType.StopwatchZero,
                                is ActivityDb.TimerType.StopwatchDaily ->
                                    TextFeatures.TimerType.Timer(pausedTimerType.seconds)
                                ActivityDb.TimerType.RestOfGoal ->
                                    TextFeatures.TimerType.Timer(
                                        DayBarsUi.buildToday().buildActivityStats(pausedActivityDb).calcRestOfGoal()
                                    )
                                is ActivityDb.TimerType.Daytime ->
                                    timerType.dayTimeUi.calcTimer()
                            }
                        }
                        is TextFeatures.TimerType.Stopwatch -> pausedTimerType
                    }
                pausedTaskData.taskDb.startInterval(
                    tfTimerType = tfTimerType,
                    activityDb = pausedTaskData.activityDb,
                )
            } else {
                IntervalDb.pauseLastInterval()
            }
        }
    }

    ///

    class InfoUi(
        private val intervalDb: IntervalDb,
    ) {

        val timerType: IntervalDb.TimerType =
            intervalDb.buildTimerType()

        val untilPickerTitle = "Until Time"
        val untilDaytimeUi: DaytimeUi = run {
            val unixTime = UnixTime(
                when (timerType) {
                    is IntervalDb.TimerType.Timer -> timerType.finishTime
                    is IntervalDb.TimerType.Stopwatch -> timerType.startTime
                }
            )
            val daytime = unixTime.time - unixTime.localDayStartTime()
            DaytimeUi.byDaytime(daytime)
        }

        val timerText = "Timer"

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
    val h: String = if (hms[0] > 0) "${hms[0]}:" else ""
    val m: String = hms[1].toString().padStart(2, '0') + ":"
    val s: String = hms[2].toString().padStart(2, '0')
    return "$h$m$s"
}

private data class PausedTaskData(
    val taskDb: TaskDb,
    val activityDb: ActivityDb,
    val timerType: TextFeatures.TimerType,
)
