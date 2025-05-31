package me.timeto.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import me.timeto.shared.NotificationAlarm
import me.timeto.shared.timeMls

object AlarmCenter {

    fun scheduleNotification(data: NotificationAlarm) {
        val requestCode: Int = when (data.type) {
            NotificationAlarm.Type.timeToBreak ->
                TimerNotificationReceiver.NOTIFICATION_ID.BREAK.id
            NotificationAlarm.Type.overdue ->
                TimerNotificationReceiver.NOTIFICATION_ID.OVERDUE.id
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
}

private fun getAlarmManager(): AlarmManager =
    App.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager
