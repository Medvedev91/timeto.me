package me.timeto.shared.data

import me.timeto.shared.*
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.db.IntervalModel
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
            defColor = ColorRgba.blue,
        )
    }

    val text: String
    val textTriggers: List<TextFeatures.Trigger>

    val note: String?
    val noteTriggers: List<TextFeatures.Trigger>
    val noteIcon: NoteIcon?

    init {
        val activityTf = activity.name.textFeatures()
        text = activityTf.textUi()
        textTriggers = activityTf.triggers
        val lastIntervalNote = lastInterval.note
        if (timerData != null && lastIntervalNote != null) {
            val noteTf = lastIntervalNote.textFeatures()
            var notePrefix = ""
            if (noteTf.fromEvent != null) {
                val unixTime = noteTf.fromEvent.unixTime
                val timeComponents = mutableListOf<UnixTime.StringComponent>()
                if (unixTime.localDay != UnixTime().localDay) {
                    timeComponents.addAll(
                        listOf(
                            UnixTime.StringComponent.dayOfMonth,
                            UnixTime.StringComponent.space,
                            UnixTime.StringComponent.month3,
                            UnixTime.StringComponent.comma,
                            UnixTime.StringComponent.space,
                        )
                    )
                }
                timeComponents.add(UnixTime.StringComponent.hhmm24)
                notePrefix = unixTime.getStringByComponents(timeComponents) + " "
            }
            note = notePrefix + noteTf.textUi(withActivityEmoji = false, withTimer = false)
            noteTriggers = noteTf.triggers
            noteIcon = when {
                noteTf.fromEvent != null -> NoteIcon.event
                else -> null
            }
        } else {
            note = null
            noteTriggers = listOf()
            noteIcon = null
        }
    }

    enum class NoteIcon {
        event
    }
}
