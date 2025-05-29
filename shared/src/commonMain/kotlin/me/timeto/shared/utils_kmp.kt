package me.timeto.shared

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import me.timeto.shared.db.*
import me.timeto.shared.db.KvDb.Companion.asDayStartOffsetSeconds
import me.timeto.shared.db.KvDb.Companion.isSendingReports
import me.timeto.shared.misc.getString
import me.timeto.shared.misc.SystemInfo
import me.timeto.shared.misc.ioScope
import me.timeto.shared.misc.time
import me.timeto.shared.misc.zlog

const val GOLDEN_RATIO = 1.618f

const val OPEN_SOURCE_URL = "https://github.com/Medvedev91/timeto.me"
const val HI_EMAIL = "hi@timeto.me"

const val prayEmoji = "🙏"

fun reportApi(
    message: String,
) {

    // Not launchEx because of recursion
    ioScope().launch {

        if (!KvDb.KEY.IS_SENDING_REPORTS.selectOrNull().isSendingReports())
            return@launch

        val title: String = when (SystemInfo.instance.os) {
            is SystemInfo.Os.Android -> "🤖 Android"
            is SystemInfo.Os.Ios -> " iOS"
            is SystemInfo.Os.Watchos -> "⌚ Watch OS"
        }

        zlog("reportApi $message")
        try {
            HttpClient().use {
                val token = KvDb.selectTokenOrNullSafe()
                it.submitForm(
                    url = "https://api.timeto.me/report",
                    formParameters = Parameters.build {
                        append("title", title)
                        append("message", message)
                    }
                ) {
                    appendSystemInfo(token)
                }
            }
        } catch (e: Throwable) {
            // todo report by fallback way
            // Cases:
            // - no internet connection
            // - todo check if domain unavailable
            // - todo check if not "ok" returned
            zlog("reportApi exception:\n$e")
        }
    }
}

//
// Ping

var pingLastDay: Int? = null
suspend fun ping(
    force: Boolean = false,
) {
    try {

        val today = UnixTime().localDay

        if (!force) {
            if (!KvDb.KEY.IS_SENDING_REPORTS.selectOrNull().isSendingReports())
                return
            if (pingLastDay == today)
                return
        }

        HttpClient().use { client ->
            val httpResponse = client.get("https://api.timeto.me/ping") {
                val token = KvDb.selectTokenOrNullSafe()
                val password = getsertTokenPassword()
                url {
                    parameters.append("password", password)
                    appendSystemInfo(token)
                }
            }
            val plainJson = httpResponse.bodyAsText()
            val j = Json.parseToJsonElement(plainJson).jsonObject
            if (j.getString("status") != "success")
                throw Exception("status != success\n$plainJson")
            val jData = j.jsonObject["data"]!!.jsonObject
            KvDb.KEY.TOKEN.upsertString(jData.getString("token"))
            KvDb.KEY.FEEDBACK_SUBJECT.upsertString(jData.getString("feedback_subject"))
            pingLastDay = today // After success
        }
    } catch (e: Throwable) {
        reportApi("AppVM ping() exception:\n$e")
    }
}

private suspend fun getsertTokenPassword(): String {
    val oldPassword = KvDb.KEY.TOKEN_PASSWORD.selectStringOrNull()
    if (oldPassword != null)
        return oldPassword
    val chars = ('0'..'9') + ('a'..'z') + ('A'..'Z') + ("!@#%^&*()_+".toList())
    val newPassword = (1..15).map { chars.random() }.joinToString("")
    KvDb.KEY.TOKEN_PASSWORD.upsertString(newPassword)
    return newPassword
}

//

internal suspend fun delayToNextMinute(extraMls: Long = 1_000L) {
    val secondsToNewMinute = 60 - (time() % 60)
    delay((secondsToNewMinute * 1_000L) + extraMls)
}

fun CoroutineScope.launchEx(
    block: suspend CoroutineScope.() -> Unit,
) {
    launch {
        try {
            block()
        } catch (e: Throwable) {
            reportApi("launchEx $e\n${e.stackTraceToString()}")
        }
    }
}

fun launchExIo(block: suspend CoroutineScope.() -> Unit) =
    ioScope().launchEx(block)

fun <T> Flow<T>.onEachExIn(
    scope: CoroutineScope,
    action: suspend (T) -> Unit,
) = onEach {
    try {
        action(it)
    } catch (e: Throwable) {
        reportApi("onEachEx $e")
    }
}.launchIn(scope)

fun taskAutostartData(
    task: TaskDb,
): Pair<ActivityDb, Int>? {
    val textFeatures = task.text.textFeatures()
    val activity = textFeatures.activity ?: return null
    val timerTime = textFeatures.timer ?: return null
    return activity to timerTime
}

fun HttpRequestBuilder.appendSystemInfo(
    token: String?,
) {
    url {
        val systemInfo = SystemInfo.instance
        parameters.append("__token", token ?: "")
        parameters.append("__build", systemInfo.build.toString())
        parameters.append("__os", systemInfo.os.fullVersion)
        parameters.append("__device", systemInfo.device)
        parameters.append("__flavor", systemInfo.flavor ?: "")
    }
}

// Do not use "\\s+" because it removes line breaks.
private val duplicateSpacesRegex = " +".toRegex()
fun String.removeDuplicateSpaces() = this.replace(duplicateSpacesRegex, " ")

fun getSoundTimerExpiredFileName(withExtension: Boolean): String =
    "sound_timer_expired${if (withExtension) ".mp3" else ""}"

/**
 * File names without extension must be unique!
 * It is necessary to pass "type" separately, for iOS it's a requirement of SDK, for Android it's not used.
 * Example for emojis.json: `getResourceContent("emojis", "json")`
 * Files are stored in the Android app as native. For iOS, they are imported with links from the Android project.
 * Already created a group of files for iOS: XCode right click on "TimeTo" -> "New Group without Folder" -> "resources"
 *
 * Import to iOS:
 * - XCode right click on "resources";
 * - Add Files to "timeto";
 * - Choose file. DO NOT select "copy items if needed", DO select "Create groups".
 */
expect fun getResourceContent(file: String, type: String): String

fun Int.limitMin(value: Int) = if (this < value) value else this
fun Int.limitMax(value: Int) = if (this > value) value else this
fun Int.limitMinMax(min: Int, max: Int) = this.limitMin(min).limitMax(max)

fun Float.limitMin(value: Float) = if (this < value) value else this
fun Float.limitMax(value: Float) = if (this > value) value else this
fun Float.limitMinMax(min: Float, max: Float) = this.limitMin(min).limitMax(max)

fun <T> MutableMap<T, Int>.incOrSet(key: T, value: Int) {
    set(key, (get(key) ?: 0) + value)
}

/**
 * Do use only elements from the array, otherwise Exception
 */
fun <T> List<T>.getNextOrNull(item: T): T? {
    val index = indexOf(item)
    if (index < 0)
        throw Exception() // todo report
    return if (index + 1 == size) null else get(index + 1)
}

fun Int.toHms(
    roundToNextMinute: Boolean = false
): List<Int> {
    val time = if (!roundToNextMinute) this
    else {
        val rmd = this % 60
        if (rmd == 0) this else (this + (60 - rmd))
    }

    var secondsLeft = time
    val h = secondsLeft / 3600
    secondsLeft -= h * 3600
    val m = secondsLeft / 60
    secondsLeft -= m * 60
    return listOf(h, m, secondsLeft)
}

fun Int.toTimerHintNote(
    isShort: Boolean,
): String {
    val (h, m) = this.toHms()
    return when {
        h == 0 -> "${m}${if (isShort) "" else " min"}"
        m == 0 -> "${h}h"
        else -> "${h}:${m.toString().padStart(2, '0')}"
    }
}

//////

class UIException(val uiMessage: String) : Exception(uiMessage)

///
/// Notifications Flow

val scheduledNotificationsDataFlow = MutableSharedFlow<List<ScheduledNotificationData>>()

data class ScheduledNotificationData(
    val title: String,
    val text: String,
    val inSeconds: Int,
    val type: TYPE,
) {
    enum class TYPE {
        BREAK, OVERDUE
    }
}

suspend fun rescheduleNotifications() {
    val lastInterval = IntervalDb.selectLastOneOrNull()!!
    val inSeconds = (lastInterval.id + lastInterval.timer) - time()
    if (inSeconds <= 0)
        return

    val totalMinutes = lastInterval.timer / 60
    scheduledNotificationsDataFlow.emit(
        listOf(
            ScheduledNotificationData(
                title = "Time Is Over ⏰",
                text = if (totalMinutes == 1) "1 minute has expired" else "$totalMinutes minutes have expired",
                inSeconds = inSeconds,
                type = ScheduledNotificationData.TYPE.BREAK,
            ),
        )
    )
    /*
    val activityDb = lastInterval.getActivity()
    val pomodoroTimer = activityDb.pomodoro_timer
    if (pomodoroTimer > 0) {
        scheduledNotificationsDataFlow.emit(
            listOf(
                ScheduledNotificationData(
                    title = "Time to Break  ✅",
                    text = if (totalMinutes == 1) "1 minute has expired" else "$totalMinutes minutes have expired",
                    inSeconds = inSeconds,
                    type = ScheduledNotificationData.TYPE.BREAK,
                ),
                ScheduledNotificationData(
                    title = "Break Is Over ⏰",
                    text = "Restart or set the timer",
                    inSeconds = inSeconds + pomodoroTimer,
                    type = ScheduledNotificationData.TYPE.OVERDUE,
                ),
            )
        )
    } else {
        scheduledNotificationsDataFlow.emit(
            listOf(
                ScheduledNotificationData(
                    title = "Time Is Over ⏰",
                    text = if (totalMinutes == 1) "1 minute has expired" else "$totalMinutes minutes have expired",
                    inSeconds = inSeconds,
                    type = ScheduledNotificationData.TYPE.OVERDUE,
                ),
            )
        )
    }
    */
}

val keepScreenOnStateFlow = MutableStateFlow(false)

val backupStateFlow = MutableStateFlow<String?>(null)

///
/// Time

/**
 * todo
 * Store as a constant and update for performance. Now 3+
 * functions are being called and objects are being created.
 */
val localUtcOffset: Int
    get() = Clock.System.now().offsetIn(TimeZone.currentSystemDefault()).totalSeconds

val localUtcOffsetWithDayStart: Int
    get() = localUtcOffset - dayStartOffsetSeconds()

fun dayStartOffsetSeconds(): Int =
    KvDb.KEY.DAY_START_OFFSET_SECONDS.selectOrNullCached().asDayStartOffsetSeconds()

// todo deprecated. Use DaytimePickerUi.text
fun daytimeToString(daytime: Int): String {
    val (h, m) = daytime.toHms()
    return "$h:${m.toString().padStart(2, '0')}"
}

//////

class TimerPickerItem(
    val idx: Int,
    val seconds: Int,
    val title: String,
) {

    companion object {

        fun buildList(
            defSeconds: Int,
        ): List<TimerPickerItem> {

            val a = (1..10).map { it * 60 } + // 1 - 10 min by 1 min
                    (1..10).map { (600 + (it * 300)) } + // 15 min - 1 hour by 5 min
                    (1..138).map { (3_600 + (it * 600)) } + // 1 hour+ by 10 min
                    defSeconds

            return a.toSet().sorted().mapIndexed { idx, seconds ->

                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60

                val title = when {
                    hours == 0 -> "$minutes min"
                    minutes == 0 -> "$hours h"
                    else -> "$hours : ${minutes.toString().padStart(2, '0')}"
                }

                TimerPickerItem(
                    idx = idx,
                    seconds = seconds,
                    title = title
                )
            }
        }

        fun calcDefSeconds(
            activity: ActivityDb,
            note: String?,
        ): Int {
            if (note == null)
                return activity.timer

            // If the note contains the time, it takes priority.
            val textFeatures = note.textFeatures()
            if (textFeatures.timer != null)
                return textFeatures.timer

            return activity.timer
        }
    }
}

class TimerTimeParser(
    val seconds: Int,
    val match: String,
) {

    companion object {

        // Using IGNORE_CASE, not lowercase() to set to the "match" real string
        private val regex = "\\d+\\s?min".toRegex(RegexOption.IGNORE_CASE)

        fun parse(text: String): TimerTimeParser? {
            val match = regex.find(text)?.value ?: return null
            val seconds = match.filter { it.isDigit() }.toInt() * 60
            return TimerTimeParser(seconds, match)
        }
    }
}

//////

class Wheel<T>(
    private val items: List<T>,
) {

    private var lastIndex = -1

    fun next(): T {
        var nextIndex = lastIndex + 1
        if (nextIndex >= items.size)
            nextIndex = 0
        lastIndex = nextIndex
        return items[nextIndex]
    }
}
