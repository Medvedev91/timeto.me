package me.timeto.shared

import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb

data class IntervalUi(
    val intervalDb: IntervalDb,
    val activityDb: ActivityDb,
)
