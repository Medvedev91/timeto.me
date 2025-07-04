package me.timeto.shared

import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.GoalDb
import me.timeto.shared.db.ShortcutDb
import kotlin.math.absoluteValue

data class TextFeatures(
    val textNoFeatures: String,
    val checklistsDb: List<ChecklistDb>,
    val shortcutsDb: List<ShortcutDb>,
    val fromRepeating: FromRepeating?,
    val fromEvent: FromEvent?,
    val activityDb: ActivityDb?,
    val goalDb: GoalDb?,
    val timer: Int?,
    val pause: Pause?,
    val paused: Paused?,
    val prolonged: Prolonged?,
    val isImportant: Boolean,
) {

    fun calcTimeData(): TimeData? = when {
        fromRepeating?.time != null ->
            TimeData(UnixTime(fromRepeating.time), TimeData.TYPE.REPEATING)
        fromEvent != null ->
            TimeData(fromEvent.unixTime, TimeData.TYPE.EVENT)
        else ->
            null
    }

    fun textUi(
        withActivityEmoji: Boolean = true,
        withPausedEmoji: Boolean = false,
        withTimer: Boolean = true,
        timerPrefix: String = "",
    ): String {
        val a = mutableListOf(textNoFeatures)
        if (paused != null && withPausedEmoji)
            a.add(0, "⏸️")
        if (activityDb != null && withActivityEmoji)
            a.add(activityDb.emoji)
        if (timer != null && withTimer)
            a.add(timerPrefix + timer.toTimerHintNote(isShort = false))
        return a.joinToString(" ")
    }

    fun textWithFeatures(): String {
        val strings = mutableListOf(textNoFeatures.trim())
        if (checklistsDb.isNotEmpty())
            strings.add(checklistsDb.joinToString(" ") { "#c${it.id}" })
        if (shortcutsDb.isNotEmpty())
            strings.add(shortcutsDb.joinToString(" ") { "#s${it.id}" })
        if (fromRepeating != null)
            strings.add("#r${fromRepeating.id}_${fromRepeating.day}_${fromRepeating.time ?: ""}")
        if (fromEvent != null)
            strings.add("#e${fromEvent.unixTime.time}")
        if (activityDb != null)
            strings.add("#a${activityDb.id}")
        if (goalDb != null)
            strings.add("{{goal_${goalDb.id}}}")
        if (timer != null)
            strings.add("#t$timer")
        if (pause != null)
            strings.add("##pause_${pause.pausedTaskId}")
        if (paused != null)
            strings.add("#paused${paused.intervalId}_${paused.originalTimer}")
        if (prolonged != null)
            strings.add("##prolonged_${prolonged.originalTimer}")
        if (isImportant)
            strings.add(isImportantSubstring)
        return strings.joinToString(" ")
    }

    // Day to sync! May be different from the real one meaning "Day Start"
    // setting. "day" is used for sorting within "Today" tasks list.
    class FromRepeating(val id: Int, val day: Int, val time: Int?)

    class FromEvent(val unixTime: UnixTime)

    class Pause(val pausedTaskId: Int)

    class Paused(val intervalId: Int, val originalTimer: Int)

    class Prolonged(val originalTimer: Int)

    class TimeData(
        val unixTime: UnixTime,
        val type: TYPE,
    ) {

        val secondsLeft: Int =
            unixTime.time - time()

        val status: STATUS = when {
            secondsLeft > 3_600 -> STATUS.IN
            secondsLeft > 0 -> STATUS.SOON
            else -> STATUS.OVERDUE
        }

        ///

        fun timeText(): String {
            val components =
                if (unixTime.isToday())
                    listOf(UnixTime.StringComponent.hhmm24)
                else
                    listOf(
                        UnixTime.StringComponent.dayOfMonth,
                        UnixTime.StringComponent.space,
                        UnixTime.StringComponent.month3,
                        UnixTime.StringComponent.comma,
                        UnixTime.StringComponent.space,
                        UnixTime.StringComponent.hhmm24,
                    )
            return unixTime.getStringByComponents(components)
        }

        fun timeLeftText(): String {
            val isOverdue = status.isOverdue()
            val (h, m) = secondsLeft.absoluteValue.toHms(roundToNextMinute = !isOverdue)
            val d = h / 24
            val timeText: String = when {
                d >= 1 -> "${d}d"
                h >= 10 -> "${h}h"
                h > 0 -> if (m == 0) "${h}h" else "$h:${m.toString().padStart(2, '0')}"
                m == 0 -> "Now! 🙀"
                else -> "${m}m"
            }
            return if (isOverdue) timeText else "In $timeText"
        }

        enum class TYPE {

            EVENT, REPEATING;

            fun isEvent() = this == EVENT
            fun isRepeating() = this == REPEATING
        }

        enum class STATUS {

            IN, SOON, OVERDUE;

            fun isIn() = this == IN
            fun isSoon() = this == SOON
            fun isOverdue() = this == OVERDUE
        }
    }
}

fun String.textFeatures(): TextFeatures = parseLocal(this)

//////

private val checklistRegex = "#c(\\d{10})".toRegex()
private val shortcutRegex = "#s(\\d+)".toRegex()
private val fromRepeatingRegex = "#r(\\d{10})_(\\d{5})_(\\d{10})?".toRegex()
private val fromEventRegex = "#e(\\d{10})".toRegex()
private val activityRegex = "#a(\\d{10})".toRegex()
private val goalRegex = "\\{\\{goal_(\\d+)\\}\\}".toRegex()
private val timerRegex = "#t(\\d+)".toRegex()
private val pauseRegex = "##pause_(\\d{10})".toRegex()
private val pausedRegex = "#paused(\\d{10})_(\\d+)".toRegex()
private val prolongedRegex = "##prolonged_(\\d+)".toRegex()
private const val isImportantSubstring = "#important"

private fun parseLocal(initText: String): TextFeatures {

    var textNoFeatures = initText
    fun MatchResult.clean() {
        textNoFeatures = textNoFeatures.replace(this.value, "")
    }

    val checklists: List<ChecklistDb> = checklistRegex
        .findAll(textNoFeatures)
        .map { match ->
            val id = match.groupValues[1].toInt()
            val checklistDb: ChecklistDb =
                Cache.checklistsDb.firstOrNull { it.id == id } ?: return@map null
            match.clean()
            checklistDb
        }
        .filterNotNull()
        .toList()

    val shortcuts: List<ShortcutDb> = shortcutRegex
        .findAll(textNoFeatures)
        .map { match ->
            val id = match.groupValues[1].toInt()
            val shortcutDb: ShortcutDb =
                Cache.shortcutsDb.firstOrNull { it.id == id } ?: return@map null
            match.clean()
            shortcutDb
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

    val activity: ActivityDb? = activityRegex
        .find(textNoFeatures)?.let { match ->
            val id = match.groupValues[1].toInt()
            val activityDb: ActivityDb =
                Cache.getActivityDbByIdOrNull(id) ?: return@let null
            match.clean()
            return@let activityDb
        }

    val goalDb: GoalDb? = goalRegex
        .find(textNoFeatures)?.let { match ->
            val id: Int = match.groupValues[1].toInt()
            val goalDb: GoalDb =
                Cache.goalsDb.firstOrNull { it.id == id } ?: return@let null
            match.clean()
            return@let goalDb
        }

    val timer: Int? = timerRegex
        .find(textNoFeatures)?.let { match ->
            val time = match.groupValues[1].toInt()
            match.clean()
            return@let time
        }

    val pause: TextFeatures.Pause? = pauseRegex
        .find(textNoFeatures)?.let { match ->
            val taskId = match.groupValues[1].toInt()
            match.clean()
            return@let TextFeatures.Pause(taskId)
        }

    val paused: TextFeatures.Paused? = pausedRegex
        .find(textNoFeatures)?.let { match ->
            val intervalId = match.groupValues[1].toInt()
            val intervalTimer = match.groupValues[2].toInt()
            match.clean()
            return@let TextFeatures.Paused(intervalId, intervalTimer)
        }

    val prolonged: TextFeatures.Prolonged? = prolongedRegex
        .find(textNoFeatures)?.let { match ->
            val originalTimer = match.groupValues[1].toInt()
            match.clean()
            return@let TextFeatures.Prolonged(originalTimer)
        }

    val isImportant = isImportantSubstring in textNoFeatures
    if (isImportant)
        textNoFeatures = textNoFeatures.replace(isImportantSubstring, "")

    return TextFeatures(
        textNoFeatures = textNoFeatures.removeDuplicateSpaces().trim(),
        checklistsDb = checklists,
        shortcutsDb = shortcuts,
        fromRepeating = fromRepeating,
        fromEvent = fromEvent,
        activityDb = activity,
        goalDb = goalDb,
        timer = timer,
        pause = pause,
        paused = paused,
        prolonged = prolonged,
        isImportant = isImportant,
    )
}
