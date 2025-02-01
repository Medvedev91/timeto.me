package me.timeto.shared

import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import dbsq.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import me.timeto.appdbsq.TimetomeDB
import me.timeto.shared.db.*
import me.timeto.shared.db.KvDb.Companion.asDayStartOffsetSeconds
import me.timeto.shared.db.KvDb.Companion.isSendingReports
import me.timeto.shared.misc.SystemInfo

const val GOLDEN_RATIO = 1.618f

const val OPEN_SOURCE_URL = "https://github.com/Medvedev91/timeto.me"
const val HI_EMAIL = "hi@timeto.me"

const val developerEmoji = "👨‍💻"
const val prayEmoji = "🙏"

internal expect val REPORT_API_TITLE: String
fun reportApi(
    message: String,
    force: Boolean = false,
) {

    // Not launchEx because of recursion
    defaultScope().launch {

        if (!force && !KvDb.KEY.IS_SENDING_REPORTS.selectOrNull().isSendingReports())
            return@launch

        zlog("reportApi $message")
        try {
            HttpClient().use {
                val token = KvDb.selectTokenOrNullSafe()
                it.submitForm(
                    url = "https://api.timeto.me/report",
                    formParameters = Parameters.build {
                        append("title", REPORT_API_TITLE)
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

fun launchExDefault(block: suspend CoroutineScope.() -> Unit) =
    defaultScope().launchEx(block)

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

data class ColorRgba(
    val r: Int, val g: Int,
    val b: Int, val a: Int = 255,
) {

    companion object {

        val white = ColorRgba(255, 255, 255)
        val black = ColorRgba(0, 0, 0)
        val transparent = ColorRgba(0, 0, 0, 0)

        val red = AppleColors.Palettes.red.dark
        val green = AppleColors.Palettes.green.dark
        val blue = AppleColors.Palettes.blue.dark
        val orange = AppleColors.Palettes.orange.dark
        val purple = AppleColors.Palettes.purple.dark

        val text = ColorRgba(255, 255, 255, 238)
        val textSecondary = ColorRgba(255, 255, 255, 170)
        val tertiaryText = ColorRgba(255, 255, 255, 136)

        val bg = black
        val fg = ColorRgba(30, 30, 32)

        val dividerBg = ColorRgba(50, 50, 53)
        val dividerFg = ColorRgba(61, 61, 65)

        val sheetBg = fg
        val sheetFg = ColorRgba(44, 44, 46)
        val sheetDividerBg = dividerFg
        val sheetDividerFg = ColorRgba(68, 68, 71)

        val homeFontSecondary = ColorRgba(255, 255, 255, 128)
        val homeMenuTime = ColorRgba(255, 255, 255, 200)
        val homeFg = sheetFg

        val summaryDatePicker = sheetFg

        val tasksDropFocused = green

        fun fromRgbaString(rgbaString: String): ColorRgba =
            rgbaString.split(',').map { it.toInt() }.let {
                when (it.size) {
                    3 -> ColorRgba(it[0], it[1], it[2])
                    4 -> ColorRgba(it[0], it[1], it[2], it[3])
                    else -> {
                        reportApi("ColorRgba.fromRgbaString($rgbaString) invalid")
                        throw UIException("Invalid color")
                    }
                }
            }
    }

    fun toRgbaString() = "$r,$g,$b,$a"

    fun isEquals(r_: Int, g_: Int, b_: Int, a_: Int) =
        r == r_ && g == g_ && b == b_ && a == a_
}

/**
 * https://developer.apple.com/design/human-interface-guidelines/color
 */
object AppleColors {

    val gray1Dark = ColorRgba(142, 142, 147)
    val gray2Dark = ColorRgba(99, 99, 102)
    val gray3Dark = ColorRgba(72, 72, 74)
    val gray4Dark = ColorRgba(58, 58, 60)
    val gray5Dark = ColorRgba(44, 44, 46)

    // @formatter:off
    object Palettes {

        val red = Palette("Red", ColorRgba(255, 59, 48), ColorRgba(255, 69, 58), ColorRgba(255, 105, 97), ColorRgba(215, 0, 21))
        val orange = Palette("Orange", ColorRgba(255, 149, 0), ColorRgba(255, 159, 10), ColorRgba(255, 179, 64), ColorRgba(201, 52, 0))
        val yellow = Palette("Yellow", ColorRgba(255, 204, 0), ColorRgba(255, 214, 10), ColorRgba(255, 212, 38), ColorRgba(178, 80, 0))
        val green = Palette("Green", ColorRgba(52, 199, 89), ColorRgba(48, 209, 88), ColorRgba(48, 219, 91), ColorRgba(36, 138, 61))
        val mint = Palette("Mint", ColorRgba(0, 199, 190), ColorRgba(99, 230, 226), ColorRgba(102, 212, 207), ColorRgba(12, 129, 123))
        val teal = Palette("Teal", ColorRgba(48, 176, 199), ColorRgba(64, 200, 224), ColorRgba(93, 230, 255), ColorRgba(0, 130, 153))
        val cyan = Palette("Cyan", ColorRgba(50, 173, 230), ColorRgba(100, 210, 255), ColorRgba(112, 215, 255), ColorRgba(0, 113, 164))
        val blue = Palette("Blue", ColorRgba(0, 122, 255), ColorRgba(10, 132, 255), ColorRgba(64, 156, 255), ColorRgba(0, 64, 221))
        val indigo = Palette("Indigo", ColorRgba(88, 86, 214), ColorRgba(94, 92, 230), ColorRgba(125, 122, 255), ColorRgba(54, 52, 163))
        val purple = Palette("Purple", ColorRgba(175, 82, 222), ColorRgba(191, 90, 242), ColorRgba(218, 143, 255), ColorRgba(137, 68, 171))
        val pink = Palette("Pink", ColorRgba(255, 45, 85), ColorRgba(255, 55, 95), ColorRgba(255, 100, 130), ColorRgba(211, 15, 69))
        val brown = Palette("Brown", ColorRgba(165, 132, 94), ColorRgba(172, 142, 104), ColorRgba(181, 148, 105), ColorRgba(127, 101, 69))

        // Custom Gray
        val gray = Palette("Gray", ColorRgba(142, 142, 147), ColorRgba(142, 142, 147), ColorRgba(174, 174, 178), ColorRgba(72, 72, 74))

        val all = listOf(red, orange, yellow, green, mint, teal, cyan, blue, indigo, purple, pink, brown, gray)
    }
    // @formatter:on

    class Palette(
        val name: String,
        val light: ColorRgba,
        val dark: ColorRgba,
        val aLight: ColorRgba,
        val aDark: ColorRgba,
    )
}

//////

class UIException(val uiMessage: String) : Exception(uiMessage)

fun assertOrUIException(
    condition: Boolean,
    exText: String,
) {
    if (!condition)
        throw UIException(exText)
}

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
    val lastInterval = IntervalDb.getLastOneOrNull()!!
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

///
/// UI Alert / Confirmation / Triggers

val uiAlertFlow = MutableSharedFlow<UIAlertData>()

data class UIAlertData(
    val message: String,
)

fun showUiAlert(
    message: String,
    reportApiText: String? = null,
) {
    launchExDefault { uiAlertFlow.emit(UIAlertData(message)) }
    if (reportApiText != null)
        reportApi(reportApiText)
}

//

val uiConfirmationFlow = MutableSharedFlow<UIConfirmationData>()

data class UIConfirmationData(
    val text: String,
    val buttonText: String,
    val isRed: Boolean,
    val onConfirm: () -> Unit,
)

fun showUiConfirmation(data: UIConfirmationData) {
    launchExDefault { uiConfirmationFlow.emit(data) }
}

//

val uiShortcutFlow = MutableSharedFlow<ShortcutDb>()

val uiChecklistFlow = MutableSharedFlow<ChecklistDb>()

val keepScreenOnStateFlow = MutableStateFlow(false)

val backupStateFlow = MutableStateFlow<String?>(null)

///
/// KMP init/await

lateinit var initKmpDeferred: Deferred<Unit>

internal fun initKmp(
    sqlDriver: SqlDriver,
    systemInfo_: SystemInfo,
) {
    db = TimetomeDB(
        driver = sqlDriver,
        ActivitySQAdapter = ActivitySQ.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        ChecklistItemSQAdapter = ChecklistItemSQ.Adapter(IntColumnAdapter, IntColumnAdapter, IntColumnAdapter, IntColumnAdapter),
        ChecklistSQAdapter = ChecklistSQ.Adapter(IntColumnAdapter),
        EventSQAdapter = EventSQ.Adapter(IntColumnAdapter, IntColumnAdapter),
        EventTemplateSQAdapter = EventTemplateSQ.Adapter(IntColumnAdapter, IntColumnAdapter, IntColumnAdapter),
        IntervalSQAdapter = IntervalSQ.Adapter(IntColumnAdapter, IntColumnAdapter, IntColumnAdapter),
        RepeatingSQAdapter = RepeatingSQ.Adapter(
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
            IntColumnAdapter,
        ),
        ShortcutSQAdapter = ShortcutSQ.Adapter(IntColumnAdapter),
        TaskFolderSQAdapter = TaskFolderSQ.Adapter(IntColumnAdapter, IntColumnAdapter),
        TaskSQAdapter = TaskSQ.Adapter(IntColumnAdapter, IntColumnAdapter),
        NoteSQAdapter = NoteSQ.Adapter(IntColumnAdapter, IntColumnAdapter),
        GoalSqAdapter = GoalSq.Adapter(IntColumnAdapter, IntColumnAdapter, IntColumnAdapter, IntColumnAdapter),
    )
    systemInfo = systemInfo_
    initKmpDeferred = ioScope().async { Cache.init() }
}

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
