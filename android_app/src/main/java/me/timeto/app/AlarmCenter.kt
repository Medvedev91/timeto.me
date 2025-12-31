package me.timeto.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import me.timeto.app.NotificationsUtils.NOTIFICATION_ID_BREAK
import me.timeto.app.NotificationsUtils.NOTIFICATION_ID_NO_ACTIVITY_START
import me.timeto.app.NotificationsUtils.NOTIFICATION_ID_OVERDUE
import me.timeto.shared.NotificationAlarm
import me.timeto.shared.timeMls

object AlarmCenter {

    fun scheduleNotification(data: NotificationAlarm) {
        val requestCode: Int = when (val type = data.type) {
            NotificationAlarm.Type.TimeToBreak -> NOTIFICATION_ID_BREAK
            NotificationAlarm.Type.Overdue -> NOTIFICATION_ID_OVERDUE
            is NotificationAlarm.Type.NoActivity -> NOTIFICATION_ID_NO_ACTIVITY_START + type.day
        }

        val context = App.instance
        val intent = Intent(context, TimerNotificationReceiver::class.java)

        intent.putExtra(TimerNotificationReceiver.EXTRA_TITLE, data.title)
        intent.putExtra(TimerNotificationReceiver.EXTRA_TEXT, data.text)
        intent.putExtra(TimerNotificationReceiver.EXTRA_REQUEST_CODE, requestCode)
        intent.putExtra(TimerNotificationReceiver.EXTRA_LIVE_TITLE, data.liveActivity.dynamicIslandTitle)
        intent.putExtra(TimerNotificationReceiver.EXTRA_LIVE_FINISH_TIME, data.liveActivity.intervalDb.finishTime)
        intent.putExtra(TimerNotificationReceiver.EXTRA_LIVE_EXPIRED_STRING, data.liveActivity.intervalDb.getExpiredString())

        val pIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
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

        val requestCodes: List<Int> =
            listOf(NOTIFICATION_ID_BREAK, NOTIFICATION_ID_OVERDUE) +
                    NotificationsUtils.NOTIFICATION_ID_NO_ACTIVITY_RANGE
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
}

private fun getAlarmManager(): AlarmManager =
    App.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager
