package me.timeto.shared.vm.home.settings.buttons

import me.timeto.shared.db.GoalDb

sealed class HomeSettingsButtonType {

    data class Goal(
        val goalDb: GoalDb,
    ) : HomeSettingsButtonType()

    object Empty : HomeSettingsButtonType()
}
