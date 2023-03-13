package timeto.shared.ui

import timeto.shared.EMOJI_CALENDAR
import timeto.shared.EMOJI_REPEATING
import timeto.shared.TextFeatures
import timeto.shared.Trigger

class IntervalNoteUI(
    plainText: String,
    checkLeadingEmoji: Boolean,
) {

    val text: String
    val leadingEmoji: String?
    val triggers: List<Trigger>

    init {
        val textFeatures = TextFeatures.parse(plainText)
        triggers = textFeatures.triggers

        // todo refactor by text features repeatings/events
        val textUI = textFeatures.textUi
        if (checkLeadingEmoji) {
            val emoji = setOf(EMOJI_REPEATING, EMOJI_CALENDAR)
                .firstOrNull { emoji -> textUI.startsWith(emoji) }
            if (emoji != null) {
                text = textUI.replaceFirst(emoji, "").trim()
                leadingEmoji = emoji
            } else {
                text = textUI
                leadingEmoji = null
            }
        } else {
            text = textUI
            leadingEmoji = null
        }
    }
}
