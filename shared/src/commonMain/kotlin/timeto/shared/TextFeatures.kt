package timeto.shared

import timeto.shared.db.ActivityModel
import timeto.shared.ui.TimeUI

data class TextFeatures(
    val textNoFeatures: String,
    val triggers: List<Trigger>,
    val fromRepeating: FromRepeating?,
    val fromEvent: FromEvent?,
    val activity: ActivityModel?,
    val timer: Int?,
) {

    val timeUI: TimeUI? = when {
        fromRepeating?.time != null -> TimeUI(UnixTime(fromRepeating.time))
        fromEvent != null -> TimeUI(UnixTime(fromEvent.time))
        else -> null
    }

    val textUi = textNoFeatures +
                 (if (activity != null) " ${activity.emoji}" else "") +
                 (timer?.toTimerHintNote(isShort = false, prefix = " ") ?: "")

    fun textWithFeatures(): String {
        val strings = mutableListOf(textNoFeatures.trim())
        if (triggers.isNotEmpty())
            strings.add(triggers.joinToString(" ") { it.id })
        if (fromRepeating != null)
            strings.add("#r${fromRepeating.id}_${fromRepeating.day}_${fromRepeating.time ?: ""}")
        if (fromEvent != null)
            strings.add("#e${fromEvent.time}")
        if (activity != null)
            strings.add("#a${activity.id}")
        if (timer != null)
            strings.add("#t$timer")
        return strings.joinToString(" ")
    }

    companion object {

        fun parse(initText: String): TextFeatures = parseLocal(initText)
    }

    // Day to sync! May be different from the real one meaning "Day Start"
    // setting. "day" is used for sorting within "Today" tasks list.
    class FromRepeating(val id: Int, val day: Int, val time: Int?)

    class FromEvent(val time: Int)
}

fun String.textFeatures() = TextFeatures.parse(this)

//////

private val checklistRegex = "#c(\\d{10})".toRegex()
private val shortcutRegex = "#s(\\d{10})".toRegex()
private val fromRepeatingRegex = "#r(\\d{10})_(\\d{5})_(\\d{10})?".toRegex()
private val fromEventRegex = "#e(\\d{10})".toRegex()
private val activityRegex = "#a(\\d{10})".toRegex()
private val timerRegex = "#t(\\d+)".toRegex()

private fun parseLocal(initText: String): TextFeatures {

    var textNoFeatures = initText
    fun MatchResult.clean() {
        textNoFeatures = textNoFeatures.replace(this.value, "").trim()
    }

    val triggers = mutableListOf<Trigger>()

    val allChecklists = DI.checklists
    if (allChecklists.isNotEmpty())
        checklistRegex
            .findAll(textNoFeatures)
            .forEach { match ->
                val id = match.groupValues[1].toInt()
                val checklist = DI.getChecklistByIdOrNull(id) ?: return@forEach
                triggers.add(Trigger.Checklist(checklist))
                match.clean()
            }

    val allShortcuts = DI.shortcuts
    if (allShortcuts.isNotEmpty())
        shortcutRegex
            .findAll(textNoFeatures)
            .forEach { match ->
                val id = match.groupValues[1].toInt()
                val shortcut = DI.getShortcutByIdOrNull(id) ?: return@forEach
                triggers.add(Trigger.Shortcut(shortcut))
                match.clean()
            }

    val fromRepeating: TextFeatures.FromRepeating? = fromRepeatingRegex
        .find(textNoFeatures)?.let { match ->
            val id = match.groupValues[1].toInt()
            val day = match.groupValues[2].toInt()
            val time = match.groupValues[3].takeIf { it.isNotBlank() }?.toInt()
            match.clean()
            return@let TextFeatures.FromRepeating(id, day, time)
        }

    val fromEvent: TextFeatures.FromEvent? = fromEventRegex
        .find(textNoFeatures)?.let { match ->
            val time = match.groupValues[1].toInt()
            match.clean()
            return@let TextFeatures.FromEvent(time)
        }

    val activity: ActivityModel? = activityRegex
        .find(textNoFeatures)?.let { match ->
            val id = match.groupValues[1].toInt()
            val activity = DI.getActivityByIdOrNull(id) ?: return@let null
            match.clean()
            return@let activity
        }

    val timer: Int? = timerRegex
        .find(textNoFeatures)?.let { match ->
            val time = match.groupValues[1].toInt()
            match.clean()
            return@let time
        }

    return TextFeatures(
        textNoFeatures = textNoFeatures.removeDuplicateSpaces().trim(),
        triggers = triggers,
        fromRepeating = fromRepeating,
        fromEvent = fromEvent,
        activity = activity,
        timer = timer,
    )
}
