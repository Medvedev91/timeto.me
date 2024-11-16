package me.timeto.shared.models

import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.GoalDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.launchExIo
import me.timeto.shared.textFeatures

data class GoalUi(
    val goalDb: GoalDb,
    val activityDb: ActivityDb,
) {

    val textFeatures: TextFeatures = goalDb.note.textFeatures()
    val noteForUi: String =
        textFeatures.textNoFeatures.takeIf { it.isNotBlank() }
        ?: activityDb.name.textFeatures().textNoFeatures

    fun startInterval() {
        val timer: Int = textFeatures.timer ?: (45 * 60)
        launchExIo {
            IntervalDb.addWithValidation(
                timer = timer,
                activity = activityDb,
                note = goalDb.note,
            )
        }
    }
}
