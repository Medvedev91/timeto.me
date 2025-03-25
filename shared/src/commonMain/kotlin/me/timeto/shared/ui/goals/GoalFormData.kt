package me.timeto.shared.ui.goals

import me.timeto.shared.db.GoalDb
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote

data class GoalFormData(
    val seconds: Int,
    val period: GoalDb.Period,
    val note: String,
    val finishText: String,
) {

    companion object {

        fun fromGoalDb(goalDb: GoalDb) = GoalFormData(
            seconds = goalDb.seconds,
            period = goalDb.buildPeriod(),
            note = goalDb.note,
            finishText = goalDb.finish_text,
        )
    }

    ///

    val formListTitle: String =
        note.textFeatures().textNoFeatures

    val formListNote: String =
        "${period.note()}, ${seconds.toTimerHintNote(isShort = false)}"
}
