package me.timeto.shared

import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import me.timeto.shared.db.*
import me.timeto.shared.db.KvDb.Companion.asDayStartOffsetSeconds
import me.timeto.shared.misc.SystemInfo

const val GOLDEN_RATIO = 1.618f

const val OPEN_SOURCE_URL = "https://github.com/Medvedev91/timeto.me"
const val HI_EMAIL = "hi@timeto.me"

//

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

fun <T> MutableMap<T, Int>.incOrSet(key: T, value: Int) {
    set(key, (get(key) ?: 0) + value)
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
