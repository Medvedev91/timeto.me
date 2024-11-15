package me.timeto.shared.models

import me.timeto.shared.db.GoalDb
import me.timeto.shared.toTimerHintNote

class GoalFormUi(
    val seconds: Int,
    val period: GoalDb.Period,
    val note: String,
    val finishText: String,
) {

    companion object {

        fun fromGoalDb(goalDb: GoalDb) = GoalFormUi(
            seconds = goalDb.seconds,
            period = goalDb.buildPeriod(),
            note = goalDb.note,
            finishText = goalDb.finish_text,
        )
    }

    ///

    val durationString: String =
        seconds.toTimerHintNote(isShort = false)
}
