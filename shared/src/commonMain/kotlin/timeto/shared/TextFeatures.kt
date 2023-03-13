package timeto.shared

import timeto.shared.ui.TimeUI

data class TextFeatures(
    val textNoFeatures: String,
    val triggers: List<Trigger>,
    val fromRepeating: FromRepeating?,
    val fromEvent: FromEvent?,
    val timer: Int?,
) {

    val timeUI: TimeUI? = when {
        fromRepeating?.time != null -> TimeUI(UnixTime(fromRepeating.time))
        fromEvent != null -> TimeUI(UnixTime(fromEvent.time))
        else -> null
    }

    val textUi = textNoFeatures +
            (if (timer != null) " ${(timer / 60)} min" else "")

    fun textWithFeatures(): String {
        val strings = mutableListOf(textNoFeatures.trim())
        if (triggers.isNotEmpty())
            strings.add(triggers.joinToString(" ") { it.id })
        if (fromRepeating != null)
            strings.add(substringRepeating(fromRepeating.id, fromRepeating.day, fromRepeating.time))
        if (fromEvent != null)
            strings.add(substringEvent(fromEvent.time))
        if (timer != null)
            strings.add("#t$timer")
        return strings.joinToString(" ")
    }

    companion object {

        fun parse(initText: String): TextFeatures = parseLocal(initText)

        fun substringRepeating(id: Int, day: Int, time: Int?) = "#r${id}_${day}_${time ?: ""}"

        fun substringEvent(time: Int) = "#e$time"
    }

    // Day to sync! May be different from the real one meaning "Day Start"
    // setting. "day" is used for sorting within "Today" tasks list.
    class FromRepeating(val id: Int, val day: Int, val time: Int?)

    class FromEvent(val time: Int)
}

fun String.parseTextFeatures() = TextFeatures.parse(this)

//////

private val checklistRegex = "#c\\d{10}".toRegex()
private val shortcutRegex = "#s\\d{10}".toRegex()
private val fromRepeatingRegex = "#r(\\d{10})_(\\d{5})_(\\d{10})?".toRegex()
private val fromEventRegex = "#e(\\d{10})".toRegex()
private val timerRegex = "#t(\\d+)".toRegex()

private fun parseLocal(initText: String): TextFeatures {
    var textNoFeatures = initText

    val triggers = mutableListOf<Trigger>()

    //
    // Checklists

    val allChecklists = DI.checklists
    if (allChecklists.isNotEmpty())
        checklistRegex
            .findAll(textNoFeatures)
            .forEach {
                val id = it.value.filter { it.isDigit() }.toInt()
                allChecklists.firstOrNull { it.id == id }?.let { checklist ->
                    triggers.add(Trigger.Checklist(checklist))
                }
                textNoFeatures = textNoFeatures.replace(it.value, "").trim()
            }

    //
    // Shortcuts

    val allShortcuts = DI.shortcuts
    if (allShortcuts.isNotEmpty())
        shortcutRegex
            .findAll(textNoFeatures)
            .forEach {
                val id = it.value.filter { it.isDigit() }.toInt()
                allShortcuts.firstOrNull { it.id == id }?.let { shortcut ->
                    triggers.add(Trigger.Shortcut(shortcut))
                }
                textNoFeatures = textNoFeatures.replace(it.value, "").trim()
            }

    //
    // From Repeating

    val fromRepeating: TextFeatures.FromRepeating? = fromRepeatingRegex
        .find(textNoFeatures)?.let { match ->
            val id = match.groupValues[1].toInt()
            val day = match.groupValues[2].toInt()
            val time = match.groupValues[3].takeIf { it.isNotBlank() }?.toInt()
            textNoFeatures = textNoFeatures.replace(match.value, "").trim()
            return@let TextFeatures.FromRepeating(id, day, time)
        }

    //
    // From Event

    val fromEvent: TextFeatures.FromEvent? = fromEventRegex
        .find(textNoFeatures)?.let { match ->
            val time = match.groupValues[1].toInt()
            textNoFeatures = textNoFeatures.replace(match.value, "").trim()
            return@let TextFeatures.FromEvent(time)
        }

    //
    // Timer

    val timer: Int? = timerRegex
        .find(textNoFeatures)?.let { match ->
            val time = match.groupValues[1].toInt()
            textNoFeatures = textNoFeatures.replace(match.value, "").trim()
            return@let time
        }

    return TextFeatures(
        textNoFeatures = textNoFeatures.removeDuplicateSpaces().trim(),
        triggers = triggers,
        fromRepeating = fromRepeating,
        fromEvent = fromEvent,
        timer = timer,
    )
}
