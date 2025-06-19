package me.timeto.shared.vm.home.buttons

import me.timeto.shared.ColorRgba
import me.timeto.shared.DayBarsUi
import me.timeto.shared.HomeButtonSort
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.GoalDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.launchExIo
import me.timeto.shared.limitMax
import me.timeto.shared.textFeatures
import me.timeto.shared.timeMls
import me.timeto.shared.toHms

sealed class HomeButtonType {

    data class Goal(
        val goalDb: GoalDb,
        val goalTf: TextFeatures,
        val bgColor: ColorRgba,
        val barsGoalStats: DayBarsUi.GoalStats,
        val sort: HomeButtonSort,
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
            val timer: Int =
                barsGoalStats.calcTimer()

            val noteTf: TextFeatures =
                goalDb.note.textFeatures().copy(goalDb = goalDb)

            launchExIo {
                TaskDb.selectAsc()
                    .filter { taskDb ->
                        val tf = taskDb.text.textFeatures()
                        taskDb.isToday && (tf.paused != null) && (tf.goalDb?.id == goalDb.id)
                    }
                    .forEach { taskDb ->
                        taskDb.delete()
                    }
                IntervalDb.insertWithValidation(
                    timer = timer,
                    activityDb = goalDb.getActivityDbCached(),
                    note = noteTf.textWithFeatures(),
                )
            }
        }
    }
}

private fun buildGoalTextRight(
    goalDb: GoalDb,
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
