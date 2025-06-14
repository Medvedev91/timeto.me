package me.timeto.shared.vm.home.settings.buttons

import me.timeto.shared.db.GoalDb
import me.timeto.shared.textFeatures

sealed class HomeSettingsButtonType {

    data class Goal(
        val goalDb: GoalDb,
    ) : HomeSettingsButtonType() {

        val note: String =
            goalDb.note.textFeatures().textNoFeatures
    }

    object Empty : HomeSettingsButtonType()
}
