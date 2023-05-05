package me.timeto.shared.vm.ui

import me.timeto.shared.ColorNative
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ActivityModel
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
    val activityColorOrNull = if (textFeatures.activity == null) ColorNative.red else null

    val timerTitle = "Timer"
    val timerNote = textFeatures.timer?.toTimerHintNote(isShort = false) ?: "Not Selected"
    val timerColorOrNull = if (textFeatures.timer == null) ColorNative.red else null

    fun setActivity(activity: ActivityModel) =
        textFeatures.copy(activity = activity)

    fun setTimer(seconds: Int) =
        textFeatures.copy(timer = seconds)
}
