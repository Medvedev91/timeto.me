package me.timeto.shared

import me.timeto.shared.db.ActivityDb

data class ActivityUi(
    val activityDb: ActivityDb,
) {

    val symbol: Symbol =
        activityDb.symbolOrDefault()

    val colorRgba: ColorRgba =
        activityDb.colorRgba
}
