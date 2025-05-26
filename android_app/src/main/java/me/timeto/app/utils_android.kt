package me.timeto.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import me.timeto.app.misc.TimerNotificationReceiver
import me.timeto.shared.*
import me.timeto.shared.misc.timeMls
import java.util.*

fun Date.toUnixTime() = UnixTime((this.time / 1_000L).toInt())

fun Dp.limitMin(dp: Dp) = if (this < dp) dp else this
fun Dp.limitMax(dp: Dp) = if (this > dp) dp else this
fun Dp.goldenRatioUp() = this * GOLDEN_RATIO
fun Dp.goldenRatioDown() = this / GOLDEN_RATIO

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
