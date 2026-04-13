package me.timeto.shared.vm.home.settings.buttons

import me.timeto.shared.db.ActivityDb
import me.timeto.shared.textFeatures

sealed class HomeSettingsButtonType {

    data class Activity(
        val activityDb: ActivityDb,
    ) : HomeSettingsButtonType() {

        val note: String =
            activityDb.name.textFeatures().textNoFeatures
    }

    object Empty : HomeSettingsButtonType()
}
