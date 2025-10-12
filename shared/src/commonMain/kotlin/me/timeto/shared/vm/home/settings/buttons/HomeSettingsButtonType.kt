package me.timeto.shared.vm.home.settings.buttons

import me.timeto.shared.db.Goal2Db
import me.timeto.shared.textFeatures

sealed class HomeSettingsButtonType {

    data class Goal(
        val goalDb: Goal2Db,
    ) : HomeSettingsButtonType() {

        val note: String =
            goalDb.name.textFeatures().textNoFeatures
    }

    object Empty : HomeSettingsButtonType()
}
