package me.timeto.shared

import me.timeto.shared.db.ActivityModel
import me.timeto.shared.db.ChecklistModel
import me.timeto.shared.db.ShortcutModel
import kotlin.math.absoluteValue

data class TextFeatures(
    val textNoFeatures: String,
    val checklists: List<ChecklistModel>,
    val shortcuts: List<ShortcutModel>,
    val fromRepeating: FromRepeating?,
    val fromEvent: FromEvent?,
    val activity: ActivityModel?,
    val timer: Int?,
    val isPaused: Boolean,
) {

    val timeData: TimeData? = when {
        fromRepeating?.time != null -> TimeData(UnixTime(fromRepeating.time), false, TimeData.TYPE.REPEATING)
        fromEvent != null -> TimeData(fromEvent.unixTime, true, TimeData.TYPE.EVENT)
        else -> null
    }

    val triggers: List<Trigger> by lazy {
        checklists.map { Trigger.Checklist(it) } +
        shortcuts.map { Trigger.Shortcut(it) }
    }

    fun textUi(
        withActivityEmoji: Boolean = true,
        withTriggers: Boolean = true,
        withTimer: Boolean = true,
        timerPrefix: String = "",
    ): String {
        val a = mutableListOf(textNoFeatures)
        if (isPaused)
            a.add(0, "⏸️")
        if (activity != null && withActivityEmoji)
            a.add(activity.emoji)
        if (timer != null && withTimer)
            a.add(timerPrefix + timer.toTimerHintNote(isShort = false))
        if (withTriggers)
            triggers.forEach {
                a.add(it.emoji)
            }
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
            strings.add("#e${fromEvent.unixTime.time}")
        if (activity != null)
            strings.add("#a${activity.id}")
        if (timer != null)
            strings.add("#t$timer")
        if (isPaused)
            strings.add(isPausedTag)
        return strings.joinToString(" ")
    }

    // Day to sync! May be different from the real one meaning "Day Start"
    // setting. "day" is used for sorting within "Today" tasks list.
    class FromRepeating(val id: Int, val day: Int, val time: Int?)

    class FromEvent(val unixTime: UnixTime)

    sealed class Trigger(
        val id: String,
        val title: String,
        val emoji: String,
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
        ) : Trigger("#c${checklist.id}", checklist.name, "✅", ColorNative.green)

        class Shortcut(
            val shortcut: ShortcutModel
        ) : Trigger("#s${shortcut.id}", shortcut.name, "↗️", ColorNative.red)
    }

    class TimeData(
        val unixTime: UnixTime,
        val isImportant: Boolean,
        val type: TYPE,
    ) {

        val secondsLeft: Int = unixTime.time - time()

        val status: STATUS = when {
            secondsLeft > 3_600 -> STATUS.IN
            secondsLeft > 0 -> STATUS.NEAR
            else -> STATUS.OVERDUE
        }

        fun timeLeftText(): String = when (status) {
            STATUS.IN,
            STATUS.NEAR -> secondsInToString(secondsLeft)
            STATUS.OVERDUE -> secondsOverdueToString(secondsLeft)
        }

        enum class TYPE { EVENT, REPEATING }

        enum class STATUS { IN, NEAR, OVERDUE }
    }
}

fun List<TextFeatures.Trigger>.filterNoChecklists() =
    this.filter { it !is TextFeatures.Trigger.Checklist }

fun String.textFeatures(): TextFeatures = parseLocal(this)

//////

private val checklistRegex = "#c(\\d{10})".toRegex()
private val shortcutRegex = "#s(\\d{10})".toRegex()
private val fromRepeatingRegex = "#r(\\d{10})_(\\d{5})_(\\d{10})?".toRegex()
private val fromEventRegex = "#e(\\d{10})".toRegex()
private val activityRegex = "#a(\\d{10})".toRegex()
private val timerRegex = "#t(\\d+)".toRegex()
private const val isPausedTag = "#is_paused"

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
            return@let TextFeatures.FromEvent(UnixTime(time))
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

    val isPaused = textNoFeatures.contains(isPausedTag)
    if (isPaused)
        textNoFeatures = textNoFeatures.replace(isPausedTag, "")

    return TextFeatures(
        textNoFeatures = textNoFeatures.removeDuplicateSpaces().trim(),
        checklists = checklists,
        shortcuts = shortcuts,
        fromRepeating = fromRepeating,
        fromEvent = fromEvent,
        activity = activity,
        timer = timer,
        isPaused = isPaused,
    )
}

//////

private fun secondsInToString(seconds: Int): String {
    val (h, m) = seconds.toHms(roundToNextMinute = true)
    val d = h / 24
    return when {
        d >= 1 -> "In ${d.toStringEndingDays()}"
        h >= 5 -> "In ${h.toStringEndingHours()}"
        h > 0 -> "In ${h.toStringEndingHours()}${if (m == 0) "" else " $m min"}"
        else -> "In ${m.toStringEnding(true, "minute", "min")}"
    }
}

private fun secondsOverdueToString(seconds: Int): String {
    val (h, m) = seconds.absoluteValue.toHms()
    val d = h / 24
    return when {
        d >= 1 -> d.toStringEndingDays() + " overdue"
        h > 0 -> h.toStringEndingHours() + " overdue"
        m == 0 -> "Now! 🙀"
        else -> "$m min overdue"
    }
}

private fun Int.toStringEndingHours() = toStringEnding(true, "hour", "hours")
private fun Int.toStringEndingDays() = toStringEnding(true, "day", "days")
