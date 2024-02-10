package me.timeto.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.SquircleShape
import me.timeto.shared.*
import me.timeto.shared.vm.__VM
import java.util.*

fun isSDKQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun Date.toUnixTime() = UnixTime((this.time / 1_000L).toInt())

private val density by lazy { Resources.getSystem().displayMetrics.density }
fun dpToPx(dp: Float) = (dp * density).toInt()
fun pxToDp(px: Int) = (px / density)

val H_PADDING = 16.dp
val onePx = pxToDp(1).dp
val halfDp = (dpToPx(1f) / 2).dp

fun Dp.limitMin(dp: Dp) = if (this < dp) dp else this
fun Dp.limitMax(dp: Dp) = if (this > dp) dp else this
fun Dp.goldenRatioUp() = this * GOLDEN_RATIO
fun Dp.goldenRatioDown() = this / GOLDEN_RATIO

val squircleShape = SquircleShape()
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

val timerFont = FontFamily(Font(R.font.timer_font))

fun showOpenSource() {
    App.instance.startActivity(
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(OPEN_SOURCE_URL)
        }
    )
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
     * Works better. I do not know why to use 2 times pIntent, but it's okay.
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

///
/// Color

fun ColorRgba.toColor() = Color(r, g, b, a)

object c {

    val white = ColorRgba.white.toColor()
    val black = ColorRgba.black.toColor()
    val transparent = ColorRgba.transparent.toColor()

    val red = ColorRgba.red.toColor()
    val green = ColorRgba.green.toColor()
    val blue = ColorRgba.blue.toColor()
    val orange = ColorRgba.orange.toColor()
    val purple = ColorRgba.purple.toColor()

    val text = ColorRgba.text.toColor()
    val textSecondary = ColorRgba.textSecondary.toColor()

    val bg = ColorRgba.bg.toColor()
    val fg = ColorRgba.fg.toColor()

    val dividerBg = ColorRgba.dividerBg.toColor()
    val dividerFg = ColorRgba.dividerFg.toColor()

    val sheetBg = ColorRgba.sheetBg.toColor()
    val sheetFg = ColorRgba.sheetFg.toColor()
    val sheetDividerBg = ColorRgba.sheetDividerBg.toColor()
    val sheetDividerFg = ColorRgba.sheetDividerFg.toColor()

    val homeFontSecondary = ColorRgba.homeFontSecondary.toColor()
    val homeMenuTime = ColorRgba.homeMenuTime.toColor()
    val homeFg = ColorRgba.homeFg.toColor()

    val summaryDatePicker = ColorRgba.summaryDatePicker.toColor()

    val tasksDropFocused = ColorRgba.tasksDropFocused.toColor()
    val formButtonRightNoteText = ColorRgba.formButtonRightNoteText.toColor()
}
