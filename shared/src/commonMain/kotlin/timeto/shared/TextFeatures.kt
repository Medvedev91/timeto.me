package timeto.shared

import timeto.shared.db.ActivityModel
import timeto.shared.db.ChecklistModel
import timeto.shared.db.ShortcutModel
import timeto.shared.ui.TimeUI

data class TextFeatures(
    val textNoFeatures: String,
    val checklists: List<ChecklistModel>,
    val shortcuts: List<ShortcutModel>,
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

    val triggers: List<Trigger> by lazy {
        checklists.map { Trigger.Checklist(it) } +
        shortcuts.map { Trigger.Shortcut(it) }
    }

    fun textUi(
        withActivityEmoji: Boolean = true,
        withTimer: Boolean = true,
    ): String {
        val a = mutableListOf(textNoFeatures)
        if (activity != null && withActivityEmoji)
            a.add(activity.emoji)
        if (timer != null && withTimer)
            a.add(timer.toTimerHintNote(isShort = false))
        return a.joinToString(" ")
    }

    fun textWithFeatures(): String {
        val strings = mutableListOf(textNoFeatures.trim())
        if (checklists.isNotEmpty())
            strings.add(checklists.joinToString(" ") { "#c${it.id}" })
        if (shortcuts.isNotEmpty())
            strings.add(shortcuts.joinToString(" ") { "#s${it.id}" })
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

    // Day to sync! May be different from the real one meaning "Day Start"
    // setting. "day" is used for sorting within "Today" tasks list.
    class FromRepeating(val id: Int, val day: Int, val time: Int?)

    class FromEvent(val time: Int)

    sealed class Trigger(
        val id: String,
        val title: String,
        val color: ColorNative,
    ) {

        fun performUI() {
            val _when = when (this) {
                is Checklist -> launchExDefault { checklist.performUI() }
                is Shortcut -> launchExDefault { shortcut.performUI() }
            }
        }

        class Checklist(
            val checklist: ChecklistModel
        ) : Trigger("#c${checklist.id}", checklist.name, ColorNative.green)

        class Shortcut(
            val shortcut: ShortcutModel
        ) : Trigger("#s${shortcut.id}", shortcut.name, ColorNative.red)
    }
}

fun String.textFeatures(): TextFeatures = parseLocal(this)

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

    val checklists: List<ChecklistModel> = checklistRegex
        .findAll(textNoFeatures)
        .map { match ->
            val id = match.groupValues[1].toInt()
            val checklist = DI.getChecklistByIdOrNull(id) ?: return@map null
            match.clean()
            checklist
        }
        .filterNotNull()
        .toList()

    val shortcuts: List<ShortcutModel> = shortcutRegex
        .findAll(textNoFeatures)
        .map { match ->
            val id = match.groupValues[1].toInt()
            val shortcut = DI.getShortcutByIdOrNull(id) ?: return@map null
            match.clean()
            shortcut
        }
        .filterNotNull()
        .toList()

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
        checklists = checklists,
        shortcuts = shortcuts,
        fromRepeating = fromRepeating,
        fromEvent = fromEvent,
        activity = activity,
        timer = timer,
    )
}
