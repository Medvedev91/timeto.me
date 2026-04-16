package me.timeto.shared.vm.home.buttons

import me.timeto.shared.ColorRgba
import me.timeto.shared.DayBarsUi
import me.timeto.shared.HomeButtonSort
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.launchExIo
import me.timeto.shared.limitMax
import me.timeto.shared.textFeatures
import me.timeto.shared.timeMls
import me.timeto.shared.toHms
import me.timeto.shared.toTimerHintNote
import kotlin.math.absoluteValue

sealed class HomeButtonType {

    data class Activity(
        val activityDb: ActivityDb,
        val activityTf: TextFeatures,
        val bgColor: ColorRgba,
        val barsActivityStats: DayBarsUi.ActivityStats,
        val sort: HomeButtonSort,
        val timerHintUi: List<TimerHintUi>,
        val childActivitiesUi: List<ChildActivityUi>,
        val update: Long = timeMls(),
    ) : HomeButtonType() {

        val elapsedSeconds: Int =
            barsActivityStats.calcElapsedSeconds()

        val goalType: ActivityDb.GoalType? =
            activityDb.buildGoalTypeOrNull()

        val isCompleted: Boolean

        val timerPickerTitle: String =
            activityTf.textNoFeatures

        val fullText: String

        val leftText: String

        val rightText: String

        val progressRatio: Float

        val restOfGoalUi: RestOfGoalUi? = run {
            val goalTimer = (goalType as? ActivityDb.GoalType.Timer) ?: return@run null
            val restOfGoalSeconds: Int = goalTimer.seconds - elapsedSeconds
            RestOfGoalUi(
                title = listOf(
                    if (restOfGoalSeconds < 0) "Overdue by " else "Rest of Goal - ",
                    restOfGoalSeconds.absoluteValue.toTimerHintNote(isShort = false)
                ).joinToString(""),
                goalTimer = goalTimer,
            )
        }

        init {
            val note: String = activityTf.textNoFeatures
            fullText = "$note ${prepTimerStringFor1hPlus(elapsedSeconds)}"
            leftText = run {
                if (elapsedSeconds <= 0)
                    return@run note
                if (sort.size <= 2)
                    return@run note
                if (elapsedSeconds < 60)
                    return@run "$note ${elapsedSeconds}${if (sort.size >= 4) " sec" else "s"}"
                if (elapsedSeconds < 3_600)
                    return@run "$note ${elapsedSeconds / 60}${if (sort.size >= 4) " min" else "m"}"
                fullText
            }

            when (goalType) {
                is ActivityDb.GoalType.Timer -> {
                    val secondsLeft: Int = goalType.seconds - elapsedSeconds
                    isCompleted = secondsLeft <= 0
                    progressRatio =
                        if (isCompleted) 1f
                        else elapsedSeconds.limitMax(goalType.seconds).toFloat() / goalType.seconds
                    rightText =
                        if (isCompleted) ""
                        else buildGoalTimerText(seconds = secondsLeft, sort = sort)
                }
                is ActivityDb.GoalType.Counter -> {
                    val goalCount: Int = goalType.count
                    val actualCount: Int = barsActivityStats.barsCount
                    isCompleted = actualCount >= goalCount
                    progressRatio = if (isCompleted) 1f else actualCount.toFloat() / goalCount
                    rightText = if (isCompleted) "" else "$actualCount/$goalCount"
                }
                ActivityDb.GoalType.Checklist -> {
                    val checklistItemsDb: List<ChecklistItemDb> =
                        activityTf.checklistsDb.flatMap { it.getItemsCached() }
                    val totalCount: Int = checklistItemsDb.size
                    val completedCount: Int = checklistItemsDb.count { it.isChecked }
                    isCompleted = totalCount == completedCount
                    progressRatio = if (isCompleted) 1f else completedCount.toFloat() / totalCount
                    rightText = if (isCompleted) "" else "$completedCount/$totalCount"
                }
                null -> {
                    isCompleted = false
                    progressRatio = 1f
                    rightText = ""
                }
            }
        }

        fun recalculateUiIfNeeded(): Activity? {
            if (barsActivityStats.activeTimeFrom == null)
                return null
            return this.copy(update = timeMls())
        }

        fun onBarPressedOrNeedTimerPicker(): Boolean {
            return onBarPressedOrNeedTimerPickerLocal(
                activityDb = activityDb,
                onRestOfGoal = {
                    activityDb.startTfTimer(barsActivityStats.calcRestOfGoalTfTimerType())
                },
                onStopwatchDaily = {
                    activityDb.startStopwatch(startSeconds = barsActivityStats.calcElapsedSeconds())
                },
            )
        }

        fun startForSeconds(seconds: Int) {
            launchExIo {
                activityDb.startTimer(seconds)
            }
        }

        fun startRestOfGoal() {
            launchExIo {
                activityDb.startTfTimer(barsActivityStats.calcRestOfGoalTfTimerType())
            }
        }

        ///

        data class ChildActivityUi(
            val activityDb: ActivityDb,
        ) {

            val title: String =
                activityDb.name.textFeatures().textNoFeatures

            fun startOrNeedTimerPicker(): Boolean {
                return onBarPressedOrNeedTimerPickerLocal(
                    activityDb = activityDb,
                    onRestOfGoal = {
                        activityDb.startTfTimer(
                            DayBarsUi.buildToday().buildActivityStats(activityDb).calcRestOfGoalTfTimerType()
                        )
                    },
                    onStopwatchDaily = {
                        activityDb.startStopwatch(
                            startSeconds = DayBarsUi.buildToday().buildActivityStats(activityDb).calcElapsedSeconds(),
                        )
                    },
                )
            }

            fun startForSeconds(seconds: Int) {
                launchExIo {
                    activityDb.startTimer(seconds)
                }
            }
        }

        data class TimerHintUi(
            val activityDb: ActivityDb,
            val timer: Int,
        ) {

            val title: String =
                timer.toTimerHintNote(isShort = false)

            fun onTap() {
                launchExIo {
                    activityDb.startTimer(timer)
                }
            }
        }

        data class RestOfGoalUi(
            val title: String,
            val goalTimer: ActivityDb.GoalType.Timer,
        )
    }
}

private fun onBarPressedOrNeedTimerPickerLocal(
    activityDb: ActivityDb,
    onRestOfGoal: suspend () -> Unit,
    onStopwatchDaily: suspend () -> Unit,
): Boolean {
    when (val timerType = activityDb.buildTimerType()) {
        ActivityDb.TimerType.TimerPicker -> {
            return false
        }
        ActivityDb.TimerType.RestOfGoal -> {
            launchExIo { onRestOfGoal() }
            return true
        }
        ActivityDb.TimerType.StopwatchZero -> {
            launchExIo { activityDb.startStopwatch(0) }
            return true
        }
        ActivityDb.TimerType.StopwatchDaily -> {
            launchExIo { onStopwatchDaily() }
            return true
        }
        is ActivityDb.TimerType.FixedTimer -> {
            launchExIo { activityDb.startTimer(timerType.timer) }
            return true
        }
        is ActivityDb.TimerType.Daytime -> {
            launchExIo { activityDb.startTimer(timerType.dayTimeUi.calcTimer().seconds) }
            return true
        }
    }
}

private fun buildGoalTimerText(
    seconds: Int,
    sort: HomeButtonSort,
): String {
    if (seconds < 60)
        return "${seconds}${if (sort.size >= 4) " sec" else "s"}"
    if (seconds < 3_600)
        return "${seconds / 60}${if (sort.size >= 4) " min" else ""}"
    return prepTimerStringFor1hPlus(seconds)
}

private fun prepTimerStringFor1hPlus(seconds: Int): String {
    val (h, m, _) = seconds.toHms()
    return when {
        m == 0 -> "${h}h"
        else -> "${h}:${m.toString().padStart(2, '0')}"
    }
}
