package me.timeto.shared.vm.home.buttons

import me.timeto.shared.ColorRgba
import me.timeto.shared.DayBarsUi
import me.timeto.shared.HomeButtonSort
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.launchExIo
import me.timeto.shared.limitMax
import me.timeto.shared.timeMls
import me.timeto.shared.toHms
import me.timeto.shared.toTimerHintNote
import kotlin.math.absoluteValue

sealed class HomeButtonType {

    data class Goal(
        val goalDb: Goal2Db,
        val goalTf: TextFeatures,
        val bgColor: ColorRgba,
        val barsGoalStats: DayBarsUi.GoalStats,
        val sort: HomeButtonSort,
        val timerHintUi: List<TimerHintUi>,
        val update: Long = timeMls(),
    ) : HomeButtonType() {

        val elapsedSeconds: Int =
            barsGoalStats.calcElapsedSeconds()

        val fullText: String

        val leftText: String

        val rightText: String = buildGoalTextRight(
            goalDb = goalDb,
            elapsedSeconds = elapsedSeconds,
            sort = sort,
        )

        val progressRatio: Float =
            elapsedSeconds.limitMax(goalDb.seconds).toFloat() / goalDb.seconds

        // region Rest of Goal

        val restOfGoalSeconds: Int =
            goalDb.seconds - elapsedSeconds

        val restOfGoalTitle: String = listOf(
            if (restOfGoalSeconds < 0) "Overdue by " else "Rest of Goal - ",
            restOfGoalSeconds.absoluteValue.toTimerHintNote(isShort = true)
        ).joinToString("")

        // endregion

        init {
            val note: String = goalTf.textNoFeatures
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

        fun recalculateUiIfNeeded(): Goal? {
            if (barsGoalStats.activeTimeFrom == null)
                return null
            return this.copy(update = timeMls())
        }

        fun startInterval() {
            launchExIo {
                goalDb.startInterval(barsGoalStats.calcTimer())
            }
        }

        fun startForSeconds(seconds: Int) {
            launchExIo {
                goalDb.startInterval(seconds)
            }
        }

        fun startRestOfGoal() {
            launchExIo {
                goalDb.startInterval(barsGoalStats.calcTimer())
            }
        }

        ///

        data class TimerHintUi(
            val goalDb: Goal2Db,
            val timer: Int,
        ) {

            val title: String =
                timer.toTimerHintNote(isShort = false)

            fun onTap() {
                launchExIo {
                    goalDb.startInterval(timer)
                }
            }
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
