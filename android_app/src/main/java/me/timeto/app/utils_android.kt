package me.timeto.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.MySquircleShape
import me.timeto.app.ui.c
import me.timeto.shared.*
import me.timeto.shared.vm.__VM
import java.util.*

fun isSDKQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun Date.toUnixTime() = UnixTime((this.time / 1_000L).toInt())

private val density by lazy { Resources.getSystem().displayMetrics.density }
fun dpToPx(dp: Float) = (dp * density).toInt()
fun pxToDp(px: Int) = (px / density)
val onePx = pxToDp(1).dp

fun Dp.limitMin(dp: Dp) = if (this < dp) dp else this
fun Dp.limitMax(dp: Dp) = if (this > dp) dp else this
fun Dp.goldenRatioUp() = this * GOLDEN_RATIO
fun Dp.goldenRatioDown() = this / GOLDEN_RATIO

val squircleShape = MySquircleShape()
val roundedShape = RoundedCornerShape(99.dp)

fun MutableState<Boolean>.setTrue() {
    value = true
}

fun MutableState<Boolean>.setFalse() {
    value = false
}

fun Modifier.conditional(
    condition: Boolean,
    modifier: Modifier.() -> Modifier,
): Modifier {
    return if (condition) then(modifier(Modifier)) else this
}

///
/// Color

fun ColorRgba.toColor() = Color(r, g, b, a)

@Composable
fun ColorNative.toColor() = when (this) {
    ColorNative.red -> c.red
    ColorNative.green -> c.green
    ColorNative.blue -> c.blue
    ColorNative.orange -> c.orange
    ColorNative.purple -> c.purple
    ColorNative.white -> c.white
    ColorNative.black -> c.black
    ColorNative.text -> c.text
    ColorNative.textSecondary -> c.textSecondary
    ColorNative.transparent -> c.transparent
    ColorNative.timerTitleDefault -> c.timerTitleDefault
}

fun colorFromRgbaString(colorRgba: String) = colorRgba
    .split(",")
    .map { it.toInt() }
    .let {
        Color(it[0], it[1], it[2], it.getOrNull(3) ?: 255)
    }

//////

fun scheduleNotification(data: ScheduledNotificationData) {
    val requestCode = when (data.type) {
        ScheduledNotificationData.TYPE.BREAK -> TimerNotificationReceiver.NOTIFICATION_ID_BREAK
        ScheduledNotificationData.TYPE.OVERDUE -> TimerNotificationReceiver.NOTIFICATION_ID_OVERDUE
    }

    val context = App.instance
    val intent = Intent(context, TimerNotificationReceiver::class.java)

    intent.putExtra(TimerNotificationReceiver.EXTRA_TITLE, data.title)
    intent.putExtra(TimerNotificationReceiver.EXTRA_TEXT, data.text)
    intent.putExtra(TimerNotificationReceiver.EXTRA_REQUEST_CODE, requestCode)

    val pIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    /**
     * setExactAndAllowWhileIdle(), can be delayed for 10 minutes.
     *
     * Based on https://medium.com/@igordias/75c409f3bde0 use setAlarmClock().
     * Works better. I do not know why to use 2 times pIntent, but it's okey.
     */
    val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val alarmInfo = AlarmManager.AlarmClockInfo(timeMls() + (data.inSeconds * 1_000L), pIntent)
    alarm.setAlarmClock(alarmInfo, pIntent)
}

@Composable
fun <State, VM : __VM<State>> rememberVM(
    key1: Any? = null,
    key2: Any? = null,
    key3: Any? = null,
    block: () -> VM,
): Pair<VM, State> {
    val vm = remember(key1, key2, key3) {
        block()
    }
    DisposableEffect(key1, key2, key3) {
        vm.onAppear()
        onDispose {
            vm.onDisappear()
        }
    }
    return vm to vm.state.collectAsState().value
}

///
/// Vibration

private var vibrateOneShotLastMillis: Long = 0

fun vibrateShort() = vibrateOneShot(40)

fun vibrateLong() = vibrateOneShot(70)

fun vibrateOneShot(duration: Long) {
    if ((timeMls() - vibrateOneShotLastMillis) < (duration * 1.5))
        return
    val vibrator = App.instance.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    vibrateOneShotLastMillis = timeMls()
}

///
///

@Composable
fun VStack(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

@Composable
fun HStack(
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
        content = content,
    )
}

@Composable
fun ZStack(
    modifier: Modifier,
) {
    Box(modifier = modifier)
}

@Composable
fun ZStack(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints,
        content = content,
    )
}
