package me.timeto.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.misc.TimerNotificationReceiver
import me.timeto.app.ui.SquircleShape
import me.timeto.shared.*
import me.timeto.shared.misc.timeMls
import java.util.*

fun isSDKQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun Date.toUnixTime() = UnixTime((this.time / 1_000L).toInt())

private val density: Float = Resources.getSystem().displayMetrics.density
fun dpToPx(dp: Float): Int = (dp * density).toInt()
fun pxToDp(px: Int): Float = (px / density)

val H_PADDING = 16.dp
val H_PADDING_HALF = H_PADDING / 2
val onePx = pxToDp(1).dp

val halfDpFloor = pxToDp(dpToPx(1f) / 2).dp // -=
val halfDpCeil = 1.dp - halfDpFloor // +=

fun Dp.limitMin(dp: Dp) = if (this < dp) dp else this
fun Dp.limitMax(dp: Dp) = if (this > dp) dp else this
fun Dp.goldenRatioUp() = this * GOLDEN_RATIO
fun Dp.goldenRatioDown() = this / GOLDEN_RATIO

val squircleShape = SquircleShape(12.dp)
val roundedShape = RoundedCornerShape(99.dp)

val timerFont = FontFamily(Font(R.font.timer_font))

fun showOpenSource() {
    App.instance.startActivity(
        Intent(Intent.ACTION_VIEW).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse(OPEN_SOURCE_URL)
        }
    )
}

fun askAQuestion(
    subject: String,
) {
    App.instance.startActivity(
        Intent(Intent.ACTION_VIEW).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse("mailto:${HI_EMAIL}?subject=$subject")
        }
    )
}

//
// Notification / Alarms

private fun getAlarmManager(): AlarmManager =
    App.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager

fun scheduleNotification(data: ScheduledNotificationData) {
    val requestCode = when (data.type) {
        ScheduledNotificationData.TYPE.BREAK -> TimerNotificationReceiver.NOTIFICATION_ID.BREAK.id
        ScheduledNotificationData.TYPE.OVERDUE -> TimerNotificationReceiver.NOTIFICATION_ID.OVERDUE.id
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
    val alarm = getAlarmManager()
    val alarmInfo = AlarmManager.AlarmClockInfo(timeMls() + (data.inSeconds * 1_000L), pIntent)
    alarm.setAlarmClock(alarmInfo, pIntent)
}

fun cancelAllAlarms() {
    val context = App.instance
    val intent = Intent(context, TimerNotificationReceiver::class.java)
    val alarm = getAlarmManager()

    val requestCodes: List<Int> = TimerNotificationReceiver.NOTIFICATION_ID.entries.map { it.id }
    requestCodes.forEach { requestCode ->
        val pIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarm.cancel(pIntent)
    }
}

////

/// todo use Haptic
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

    val gray3 = AppleColors.gray3Dark.toColor()

    val text = ColorRgba.text.toColor()
    val textSecondary = ColorRgba.textSecondary.toColor()
    val tertiaryText = ColorRgba.tertiaryText.toColor()

    val bg = ColorRgba.bg.toColor()
    val fg = ColorRgba.fg.toColor()

    val divider = ColorRgba.divider.toColor()

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
}
