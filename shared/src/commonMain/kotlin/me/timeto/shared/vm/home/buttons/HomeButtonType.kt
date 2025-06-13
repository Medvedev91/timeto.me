package me.timeto.shared.vm.home.buttons

import me.timeto.shared.ColorRgba
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.GoalDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.launchExIo
import me.timeto.shared.limitMax
import me.timeto.shared.textFeatures
import me.timeto.shared.time
import me.timeto.shared.timeMls
import me.timeto.shared.toTimerHintNote

sealed class HomeButtonType {

    data class Goal(
        val goalDb: GoalDb,
        val goalTf: TextFeatures,
        val bgColor: ColorRgba,
        val intervalsSeconds: Int,
        val activeTimeFrom: Int?,
        val update: Long = timeMls(),
    ) : HomeButtonType() {

        val elapsedSeconds: Int =
            intervalsSeconds + (activeTimeFrom?.let { time() - it } ?: 0)

        val textLeft: String = buildTextLeft(
            note = goalTf.textNoFeatures,
            elapsedSeconds = elapsedSeconds,
        )

        val textRight: String = buildTextRight(
            goalDb = goalDb,
            elapsedSeconds = elapsedSeconds,
        )

        val progressRatio: Float =
            elapsedSeconds.limitMax(goalDb.seconds).toFloat() / goalDb.seconds

        fun recalculateUiIfNeeded(): Goal? {
            if (activeTimeFrom == null)
                return null
            return this.copy(update = timeMls())
        }

        fun startInterval() {
            val timer: Int = run {
                val goalTimer = goalDb.timer
                if (goalTimer > 0)
                    return@run goalTimer
                val secondsLeft: Int = goalDb.seconds - elapsedSeconds
                if (secondsLeft > 0)
                    return@run secondsLeft
                45 * 60
            }

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

private fun buildTextLeft(
    note: String,
    elapsedSeconds: Int,
): String {
    if (elapsedSeconds == 0)
        return note
    val rem: Int = elapsedSeconds % 60
    val secondsToUi: Int = if (rem == 0) elapsedSeconds else (elapsedSeconds + (60 - rem))
    return "$note ${secondsToUi.toTimerHintNote(isShort = false)}"
}

private fun buildTextRight(
    goalDb: GoalDb,
    elapsedSeconds: Int,
): String {
    val timeLeft: Int = goalDb.seconds - elapsedSeconds
    if (timeLeft > 0)
        return timeLeft.toTimerHintNote(isShort = false)
    if (timeLeft == 0)
        return goalDb.finish_text
    return "+ ${(timeLeft * -1).toTimerHintNote(isShort = false)} ${goalDb.finish_text}"
}
