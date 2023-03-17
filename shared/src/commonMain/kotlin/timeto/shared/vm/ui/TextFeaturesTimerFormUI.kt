package timeto.shared.vm.ui

import timeto.shared.ColorNative
import timeto.shared.TextFeatures
import timeto.shared.db.ActivityModel
import timeto.shared.textFeatures
import timeto.shared.toTimerHintNote

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
