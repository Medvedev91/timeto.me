package timeto.shared

data class TextFeatures(
    val textNoFeatures: String,
    val triggers: List<Trigger>,
    val daytime: Int? = null,
) {

    fun uiText(): String {
        return textNoFeatures
    }

    fun textWithFeatures(): String {
        val strings = mutableListOf(textNoFeatures.trim())
        if (triggers.isNotEmpty())
            strings.add(triggers.joinToString(" ") { it.id })
        if (daytime != null)
            strings.add(daytimeToString(daytime))
        return strings.joinToString(" ")
    }

    companion object {

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

            val daytime: Int? = daytimeRegex
                .find(textNoFeatures)?.let { match ->
                    val hour = match.groupValues[2].toInt()
                    val minute = match.groupValues[3].toInt()
                    if (hour > 23 || minute > 59)
                        return@let null
                    textNoFeatures = textNoFeatures.replace(match.value, "").trim()
                    return@let (hour * 3_600) + (minute * 60)
                }

            return TextFeatures(
                textNoFeatures = textNoFeatures.removeDuplicateSpaces().trim(),
                triggers = triggers,
                daytime = daytime,
            )
        }

        fun daytimeToString(daytime: Int): String {
            val hms = secondsToHms(daytime)
            return "${hms[0]}:${hms[1].toString().padStart(2, '0')}"
        }
    }
}

private val checklistRegex = "#c\\d{10}".toRegex()
private val shortcutRegex = "#s\\d{10}".toRegex()
private val daytimeRegex = "(^|\\s)(\\d?\\d):(\\d\\d)(\\s|$)".toRegex()
