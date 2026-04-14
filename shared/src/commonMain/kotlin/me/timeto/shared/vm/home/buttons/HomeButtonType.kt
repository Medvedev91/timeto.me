package me.timeto.shared.vm.home.buttons

import me.timeto.shared.ColorRgba
import me.timeto.shared.DayBarsUi
import me.timeto.shared.HomeButtonSort
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ActivityDb
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

        val timerPickerTitle: String =
            activityTf.textNoFeatures

        val fullText: String

        val leftText: String

        val rightText: String = buildActivityTextRight(
            activityDb = activityDb,
            elapsedSeconds = elapsedSeconds,
            sort = sort,
        )

        val progressRatio: Float = when (goalType) {
            is ActivityDb.GoalType.Timer -> elapsedSeconds.limitMax(goalType.seconds).toFloat() / goalType.seconds
            else -> 1f
        }

        val isCompletedAsChecklist: Boolean = run {
            val checklistsDb = activityTf.checklistsDb
            checklistsDb.isNotEmpty() && checklistsDb.flatMap { it.getItemsCached() }.all { it.isChecked }
        }

        // region Rest of Goal

        val restOfGoalSeconds: Int =
            goalDb.seconds - elapsedSeconds

        val restOfGoalTitle: String = listOf(
            if (restOfGoalSeconds < 0) "Overdue by " else "Rest of Goal - ",
            restOfGoalSeconds.absoluteValue.toTimerHintNote(isShort = false)
        ).joinToString("")

        // endregion

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
                    activityDb.startTimer(seconds = barsActivityStats.calcRestOfGoal())
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
                activityDb.startTimer(barsActivityStats.calcRestOfGoal())
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
                        activityDb.startTimer(
                            seconds = DayBarsUi.buildToday().buildActivityStats(activityDb).calcRestOfGoal()
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

private fun buildGoalTextRight(
    goalDb: Goal2Db,
    elapsedSeconds: Int,
    sort: HomeButtonSort,
): String {
    // Not Finished
    val timeLeft: Int = goalDb.seconds - elapsedSeconds
    if (timeLeft == 0)
        return goalDb.finish_text
    if (timeLeft > 0)
        return buildGoalTextRightTimer(timeLeft, sort)
    // Finished
    val timeLeftAbs: Int = timeLeft * -1
    if (timeLeftAbs < 60)
        return goalDb.finish_text
    val timerString = buildGoalTextRightTimer(timeLeftAbs, sort)
    val isShort: Boolean = sort.size <= 3
    return "+${timerString}${if (isShort) "" else " ${goalDb.finish_text}"}"
}

private fun buildGoalTextRightTimer(
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
