package me.timeto.shared

import me.timeto.shared.db.Goal2Db
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
    val prolongText: String?

    ///

    private val intervalNoteTf: TextFeatures? =
        intervalDb.note?.textFeatures()

    private val goalDb: Goal2Db =
        intervalDb.selectGoalDbCached()

    private val pausedTaskData: PausedTaskData? = run {
        if (!goalDb.isOther)
            return@run null
        val pausedTaskId: Int =
            intervalNoteTf?.pause?.pausedTaskId ?: return@run null
        val pausedTask: TaskDb =
            todayTasksDb.firstOrNull { it.id == pausedTaskId } ?: return@run null
        val pausedTaskTf: TextFeatures =
            pausedTask.text.textFeatures()
        val pausedTaskTimer: Int =
            pausedTaskTf.paused?.originalTimer ?: return@run null
        val pausedGoalDb: Goal2Db =
            pausedTaskTf.goalDb ?: return@run null
        PausedTaskData(
            taskDb = pausedTask,
            goalDb = pausedGoalDb,
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

        note = (intervalNoteTf ?: goalDb.name.textFeatures()).textUi(
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
                val goalDb: Goal2Db? =
                    pausedTaskData.taskDb.text.textFeatures().goalDb
                val timer: Int = if (goalDb == null)
                    pausedTaskData.timer
                else
                    DayBarsUi.buildToday().buildGoalStats(goalDb).calcTimer()
                pausedTaskData.taskDb.startInterval(
                    timer = timer,
                    goalDb = pausedTaskData.goalDb,
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
    val goalDb: Goal2Db,
    val timer: Int,
)
