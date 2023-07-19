package me.timeto.shared.data

import me.timeto.shared.ColorNative
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.db.IntervalModel
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.ui.TimerDataUI

class TimerTabActivityData(
    activity: ActivityModel,
    lastInterval: IntervalModel,
    isPurple: Boolean,
) {

    val timerData: TimerDataUI? = run {
        if (activity.id != lastInterval.activity_id)
            return@run null
        TimerDataUI(
            interval = lastInterval,
            isPurple = isPurple,
            defColor = ColorNative.blue,
        )
    }

    val text: String
    val note: String?

    init {
        val tfActivity = activity.name.textFeatures()
        text = tfActivity.textUi()
        val lastIntervalNote = lastInterval.note
        note = if (timerData != null && lastIntervalNote != null) {
            lastIntervalNote
                .textFeatures()
                .textUi(withActivityEmoji = false, withTimer = false)
        } else null
    }
}
