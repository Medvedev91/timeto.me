package me.timeto.shared.vm.goals.form

import me.timeto.shared.db.GoalDb
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote

data class GoalFormData(
    val goalDb: GoalDb?,
    val seconds: Int,
    val period: GoalDb.Period,
    val note: String,
    val finishText: String,
    val isEntireActivity: Boolean,
) {

    companion object {

        fun fromGoalDb(goalDb: GoalDb) = GoalFormData(
            goalDb = goalDb,
            seconds = goalDb.seconds,
            period = goalDb.buildPeriod(),
            note = goalDb.note,
            finishText = goalDb.finish_text,
            isEntireActivity = goalDb.isEntireActivity,
        )
    }

    ///

    val formListTitle: String =
        note.textFeatures().textNoFeatures

    val formListNote: String =
        "${period.note()}, ${seconds.toTimerHintNote(isShort = false)}"
}
