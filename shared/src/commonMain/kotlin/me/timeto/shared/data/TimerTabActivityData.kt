package me.timeto.shared.data

import me.timeto.shared.ColorNative
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.db.IntervalModel
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.ui.TimerDataUI

class TimerTabActivityData(
    activity: ActivityModel,
    lastInterval: IntervalModel,
) {

    val isActive = activity.id == lastInterval.activity_id
    val timerData: TimerDataUI? = run {
        if (activity.id != lastInterval.activity_id)
            return@run null
        TimerDataUI(
            interval = lastInterval,
            isPurple = false,
            defColor = ColorNative.timerTitleDefault,
        )
    }

    val listText: String
    val listNote: String?
    val triggers: List<TextFeatures.Trigger>

    init {
        val tfActivity = activity.name.textFeatures()
        listText = tfActivity.textNoFeatures
        val lastIntervalNote = lastInterval.note
        if (isActive && lastIntervalNote != null) {
            val tfNote = lastIntervalNote.textFeatures()
            listNote = tfNote.textUi(
                withActivityEmoji = false,
                withTimer = true,
                timerPrefix = "- ",
            )
            triggers = (tfNote.triggers + tfActivity.triggers).distinctBy { it.id }
        } else {
            listNote = null
            triggers = tfActivity.triggers
        }
    }
}
