package timeto.shared

import timeto.shared.vm.ui.DaytimeUI

data class TextFeatures(
    val textNoFeatures: String,
    val triggers: List<Trigger>,
    val daytime: Int? = null,
) {

    fun textUI(): String {
        return textNoFeatures
    }

    fun textWithFeatures(): String {
        val strings = mutableListOf(textUI().trim())
        if (triggers.isNotEmpty())
            strings.add(triggers.joinToString(" ") { it.id })
        val daytimeString = daytimeToStringOrNull()
        if (daytimeString != null)
            strings.add(daytimeString)
        return strings.joinToString(" ")
    }

    fun daytimeToStringOrNull(): String? = daytime?.let { daytimeToString(it) }

    fun daytimeUIOrNull(): DaytimeUI? = daytime?.let { DaytimeUI(it) }

    companion object {

        fun daytimeToString(daytime: Int): String {
            val hms = secondsToHms(daytime)
            return "${hms[0]}:${hms[1].toString().padStart(2, '0')}"
        }

        fun parse(initText: String): TextFeatures {
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
            // Daytime
            //
            // Remember, it also works for calendar events daytime

            val daytime: Int? = daytimeRegex
                .find(textNoFeatures)?.let { match ->
                    val hour = match.groupValues[3].toInt()
                    val minute = match.groupValues[4].toInt()
                    if (hour > 23 || minute > 59)
                        return@let null
                    textNoFeatures = textNoFeatures.replace(match.groupValues[2], "").trim()
                    return@let (hour * 3_600) + (minute * 60)
                }

            return TextFeatures(
                textNoFeatures = textNoFeatures.removeDuplicateSpaces().trim(),
                triggers = triggers,
                daytime = daytime,
            )
        }
    }
}

private val checklistRegex = "#c\\d{10}".toRegex()
private val shortcutRegex = "#s\\d{10}".toRegex()
private val daytimeRegex = "(^|\\s)((\\d?\\d):(\\d\\d))(\\s|$)".toRegex()
