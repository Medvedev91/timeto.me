package me.timeto.shared.vm.ui

import me.timeto.shared.ColorRgba
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote

class TextFeaturesTimerFormUI(
    val textFeatures: TextFeatures,
) {

    val activityTitle = "Activity"
    val activityNote: String = run {
        val activity = textFeatures.activity ?: return@run "Not Selected"
        "${activity.name.textFeatures().textNoFeatures}  ${activity.emoji}"
    }
    val activityColorOrNull = if (textFeatures.activity == null) ColorRgba.red else null

    val timerTitle = "Timer"
    val timerNote = textFeatures.timer?.toTimerHintNote(isShort = false) ?: "Not Selected"
    val timerColorOrNull = if (textFeatures.timer == null) ColorRgba.red else null

    fun setActivity(activity: ActivityDb) =
        textFeatures.copy(activity = activity)

    fun setTimer(seconds: Int) =
        textFeatures.copy(timer = seconds)
}
