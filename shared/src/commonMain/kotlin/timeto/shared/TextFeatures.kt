package timeto.shared

class TextFeatures(
    val textNoFeatures: String,
    val triggers: List<Trigger>,
) {

    fun textWithFeatures(): String =
        "$textNoFeatures ${triggers.joinToString(" ") { it.id }}".removeDuplicateSpaces().trim()

    companion object {

        fun parse(text: String): TextFeatures {
            val triggers = mutableListOf<Trigger>()
            var textNoFeatures = text

            val allChecklists = DI.checklists
            if (allChecklists.isNotEmpty())
                "#c\\d{10}".toRegex()
                    .findAll(text.lowercase())
                    .forEach {
                        val id = it.value.filter { it.isDigit() }.toInt()
                        allChecklists.firstOrNull { it.id == id }?.let { checklist ->
                            triggers.add(Trigger.Checklist(checklist))
                        }
                        textNoFeatures = textNoFeatures.replace(it.value, "").trim()
                    }

            val allShortcuts = DI.shortcuts
            if (allShortcuts.isNotEmpty())
                "#s\\d{10}".toRegex()
                    .findAll(text.lowercase())
                    .forEach {
                        val id = it.value.filter { it.isDigit() }.toInt()
                        allShortcuts.firstOrNull { it.id == id }?.let { shortcut ->
                            triggers.add(Trigger.Shortcut(shortcut))
                        }
                        textNoFeatures = textNoFeatures.replace(it.value, "").trim()
                    }

            return TextFeatures(
                textNoFeatures = textNoFeatures.removeDuplicateSpaces().trim(),
                triggers = triggers,
            )
        }
    }
}
