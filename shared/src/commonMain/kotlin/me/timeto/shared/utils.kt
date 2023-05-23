package me.timeto.shared

import com.squareup.sqldelight.db.SqlDriver
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import kotlinx.serialization.json.*
import me.timeto.appdbsq.TimetomeDB
import me.timeto.shared.db.*
import me.timeto.shared.db.KVModel.Companion.asDayStartOffsetSeconds

const val EMOJI_CALENDAR = "🗓"
const val EMOJI_REPEATING = "🔁"

const val BREAK_SECONDS = 5 * 60
const val GOLDEN_RATIO = 1.618f

internal lateinit var deviceData: DeviceData
var batteryLevelOrNull: Int? = null
var isBatteryChargingOrNull: Boolean? = null

fun zlog(message: Any?) = println(";; $message")

internal expect val REPORT_API_TITLE: String
fun reportApi(message: String) {
    // Not launchEx because of recursion
    defaultScope().launch {
        zlog("reportApi $message")
        try {
            HttpClient().use {
                it.submitForm(
                    url = "https://api.timeto.me/report",
                    formParameters = Parameters.build {
                        append("title", REPORT_API_TITLE)
                        append("message", message)
                    }
                ) {
                    appendDeviceData()
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

internal fun defaultScope() = CoroutineScope(SupervisorJob() + Dispatchers.Default)

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

fun launchExDefault(
    block: suspend CoroutineScope.() -> Unit,
) = defaultScope().launchEx(block)

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
    task: TaskModel,
): Pair<ActivityModel, Int>? {
    val textFeatures = task.text.textFeatures()
    val activity = textFeatures.activity ?: return null
    val timerTime = textFeatures.timer ?: return null
    return activity to timerTime
}

@Throws(SecureLocalStorage__Exception::class)
fun HttpRequestBuilder.appendDeviceData() {
    url {
        parameters.append("__token", SecureLocalStorage__Key.token.getOrNull() ?: "")
        parameters.append("__build", deviceData.build.toString())
        parameters.append("__os", deviceData.os)
        parameters.append("__device", deviceData.device)
    }
}

fun Boolean.toInt10(): Int = if (this) 1 else 0
fun Int.toBoolean10(): Boolean = this != 0
fun Boolean.toString10(): String = if (this) "1" else "0"
fun String.toBoolean10(): Boolean = this != "0"

// Do not use "\\s+" because it removes line breaks.
private val duplicateSpacesRegex = " +".toRegex()
fun String.removeDuplicateSpaces() = this.replace(duplicateSpacesRegex, " ")

fun getSoundTimeToBreakFileName(withExtension: Boolean): String =
    "sound_time_to_break${if (withExtension) ".wav" else ""}"

///
/// Json

fun JsonObject.getInt(key: String): Int = this[key]!!.jsonPrimitive.int
fun JsonObject.getString(key: String): String = this[key]!!.jsonPrimitive.content
fun JsonObject.getStringOrNull(key: String): String? = this[key]!!.jsonPrimitive.contentOrNull
fun JsonObject.getBoolean(key: String): Boolean = this[key]!!.jsonPrimitive.boolean
fun JsonObject.getIntArray(key: String): List<Int> = this[key]!!.jsonArray.map { it.jsonPrimitive.int }

fun JsonArray.getInt(index: Int): Int = this[index].jsonPrimitive.int
fun JsonArray.getIntOrNull(index: Int): Int? = this[index].jsonPrimitive.intOrNull
fun JsonArray.getString(index: Int): String = this[index].jsonPrimitive.content
fun JsonArray.getStringOrNull(index: Int): String? = this[index].jsonPrimitive.contentOrNull

fun <T> List<T>.toJsonArray() = JsonArray(
    this.map { item ->
        when (item) {
            is String -> JsonPrimitive(item)
            is Int -> JsonPrimitive(item)
            null -> JsonNull
            else -> throw Exception() // todo report
        }
    }
)

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

fun Int.max(value: Int) = if (this > value) this else value
fun Int.min(value: Int) = if (this < value) this else value
fun Float.min(v: Float) = if (this < v) this else v
fun Float.max(v: Float) = if (this > v) this else v

fun Int.limitMin(value: Int) = if (this < value) value else this
fun Int.limitMax(value: Int) = if (this > value) value else this
fun Int.limitMinMax(min: Int, max: Int) = this.limitMin(min).limitMax(max)

fun Float.limitMin(value: Float) = if (this < value) value else this
fun Float.limitMax(value: Float) = if (this > value) value else this
fun Float.limitMinMax(min: Float, max: Float) = this.limitMin(min).limitMax(max)

fun <T> MutableMap<T, Int>.plusOrSet(key: T, value: Int) {
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
    val hms = this.toHms()
    if (hms[0] > 0) {
        if (hms[1] == 0)
            return "${hms[0]}h"
        return "${hms[0]}:${hms[1].toString().padStart(2, '0')}"
    }
    return "${hms[1]}${if (isShort) "m" else " min"}"
}

fun Int.toStringEnding(withNum: Boolean, one: String, many: String): String {
    val strNum = if (this == 1) one else many
    return if (withNum) "$this $strNum" else strNum
}

data class ColorRgba(
    val r: Int, val g: Int,
    val b: Int, val a: Int = 255,
) {

    companion object {

        val white = ColorRgba(255, 255, 255, 255)
        val black = ColorRgba(0, 0, 0, 255)

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

enum class ColorNative {
    red, green, blue, orange, purple, white,
    text, textSecondary, transparent,
}

/**
 * https://developer.apple.com/design/human-interface-guidelines/color
 */
object AppleColors {

    val red = AppleColor("Red", ColorRgba(255, 59, 48), ColorRgba(255, 105, 97), ColorRgba(215, 0, 21))
    val orange = AppleColor("Orange", ColorRgba(255, 149, 0), ColorRgba(255, 179, 64), ColorRgba(201, 52, 0))
    val yellow = AppleColor("Yellow", ColorRgba(255, 204, 0), ColorRgba(255, 212, 38), ColorRgba(178, 80, 0))
    val green = AppleColor("Green", ColorRgba(52, 199, 89), ColorRgba(48, 219, 91), ColorRgba(36, 138, 61))
    val mint = AppleColor("Mint", ColorRgba(0, 199, 190), ColorRgba(102, 212, 207), ColorRgba(12, 129, 123))
    val teal = AppleColor("Teal", ColorRgba(48, 176, 199), ColorRgba(93, 230, 255), ColorRgba(0, 130, 153))
    val cyan = AppleColor("Cyan", ColorRgba(50, 173, 230), ColorRgba(112, 215, 255), ColorRgba(0, 113, 164))
    val blue = AppleColor("Blue", ColorRgba(0, 122, 255), ColorRgba(64, 156, 255), ColorRgba(0, 64, 221))
    val indigo = AppleColor("Indigo", ColorRgba(88, 86, 214), ColorRgba(125, 122, 255), ColorRgba(54, 52, 163))
    val purple = AppleColor("Purple", ColorRgba(175, 82, 222), ColorRgba(218, 143, 255), ColorRgba(137, 68, 171))
    val pink = AppleColor("Pink", ColorRgba(255, 45, 85), ColorRgba(255, 100, 130), ColorRgba(211, 15, 69))
    val brown = AppleColor("Brown", ColorRgba(165, 132, 94), ColorRgba(181, 148, 105), ColorRgba(127, 101, 69))

    // Custom Gray
    val gray = AppleColor("Gray", ColorRgba(142, 142, 147), ColorRgba(174, 174, 178), ColorRgba(72, 72, 74))

    val all = listOf(red, orange, yellow, green, mint, teal, cyan, blue, indigo, purple, pink, brown, gray)

    class AppleColor(
        val name: String,
        val default: ColorRgba,
        val light: ColorRgba,
        val dark: ColorRgba,
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
    val lastInterval = IntervalModel.getLastOneOrNull()!!
    val inSeconds = (lastInterval.id + lastInterval.deadline) - time()
    if (inSeconds <= 0)
        return

    val totalMinutes = lastInterval.deadline / 60
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
                inSeconds = inSeconds + BREAK_SECONDS,
                type = ScheduledNotificationData.TYPE.OVERDUE,
            ),
        )
    )
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

val uiShortcutFlow = MutableSharedFlow<ShortcutModel>()

val uiChecklistFlow = MutableSharedFlow<ChecklistModel>()

///
/// KMM init/await

lateinit var initKmmDeferred: Deferred<Unit>

internal fun initKmm(
    sqlDriver: SqlDriver,
    deviceData_: DeviceData,
) {
    db = TimetomeDB(sqlDriver)
    deviceData = deviceData_
    initKmmDeferred = defaultScope().async { DI.init() }
}

///
/// Swift Flow

class SwiftFlow<T>(kotlinFlow: Flow<T>) : Flow<T> by kotlinFlow {
    fun watch(block: (T) -> Unit): SwiftFlow__Cancellable {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        onEach(block).launchIn(scope)
        return object : SwiftFlow__Cancellable {
            override fun cancel() {
                scope.cancel()
            }
        }
    }
}

interface SwiftFlow__Cancellable {
    fun cancel()
}

///
/// Secure Local Storage

internal expect object SecureLocalStorage {

    @Throws(SecureLocalStorage__Exception::class)
    fun getOrNull(key: SecureLocalStorage__Key): String?

    @Throws(SecureLocalStorage__Exception::class)
    fun upsert(key: SecureLocalStorage__Key, value: String?)
}

// TRICK Do not change constants, it's used as storage keys
internal enum class SecureLocalStorage__Key {

    temp, token, token_password, feedback_subject;

    @Throws(SecureLocalStorage__Exception::class)
    fun getOrNull() = SecureLocalStorage.getOrNull(this)

    @Throws(SecureLocalStorage__Exception::class)
    fun upsert(value: String?) = SecureLocalStorage.upsert(this, value)
}

internal class SecureLocalStorage__Exception(
    override val message: String,
) : Exception(message)

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

fun time(): Int = Clock.System.now().epochSeconds.toInt()

fun timeMls(): Long = Clock.System.now().toEpochMilliseconds()

fun dayStartOffsetSeconds(): Int =
    KVModel.KEY.DAY_START_OFFSET_SECONDS.getFromDIOrNull().asDayStartOffsetSeconds()

fun daytimeToString(daytime: Int): String {
    val (h, m) = daytime.toHms()
    return "$h:${m.toString().padStart(2, '0')}"
}

//////

object FullScreenUI {

    val state = MutableStateFlow(false)

    fun isOpen() = state.value

    fun open() = state.update { true }

    fun close() = state.update { false }
}

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
            activity: ActivityModel,
            note: String?,
        ): Int {
            if (note == null)
                return activity.deadline

            // If the note contains the time, it takes priority.
            val textFeatures = note.textFeatures()
            if (textFeatures.timer != null)
                return textFeatures.timer

            // If the history contains the task
            val lastHotInterval = DI.hotIntervalsDesc.firstOrNull {
                it.activity_id == activity.id && note.lowercase() == it.note?.lowercase()
            }
            if (lastHotInterval != null)
                return lastHotInterval.deadline

            return activity.deadline
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

internal data class DeviceData(
    val build: Int,
    val os: String,
    val device: String,
)
