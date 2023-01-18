package timeto.shared

import timeto.shared.ui.TimeUI

data class TextFeatures(
    val textNoFeatures: String,
    val triggers: List<Trigger>,
    val fromRepeating: FromRepeating?,
) {

    val timeUI: TimeUI? = when {
        fromRepeating?.time != null -> TimeUI(UnixTime(fromRepeating.time))
        else -> null
    }

    ///

    fun textUI(): String {
        return textNoFeatures
    }

    fun textWithFeatures(): String {
        val strings = mutableListOf(textUI().trim())
        if (triggers.isNotEmpty())
            strings.add(triggers.joinToString(" ") { it.id })
        if (fromRepeating != null)
            strings.add(substringFromRepeating(fromRepeating.day, fromRepeating.time))
        return strings.joinToString(" ")
    }

    companion object {

        fun parse(initText: String): TextFeatures = parseLocal(initText)

        fun substringFromRepeating(day: Int, time: Int?) = "#r${day}_${time ?: ""}"
    }

    // Day to sync! May be different from the real one meaning "Day Start"
    // setting. "day" is used for sorting within "Today" tasks list.
    class FromRepeating(val day: Int, val time: Int?)
}

//////

private val checklistRegex = "#c\\d{10}".toRegex()
private val shortcutRegex = "#s\\d{10}".toRegex()
private val fromRepeatingRegex = "#r(\\d{5})_(\\d{10})?".toRegex()

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
            val day = match.groupValues[1].toInt()
            val time = match.groupValues.getOrNull(2)?.toInt()
            textNoFeatures = textNoFeatures.replace(match.value, "").trim()
            return@let TextFeatures.FromRepeating(day, time)
        }

    return TextFeatures(
        textNoFeatures = textNoFeatures.removeDuplicateSpaces().trim(),
        triggers = triggers,
        fromRepeating = fromRepeating,
    )
}
