package me.timeto.app

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import me.timeto.shared.LiveActivity
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.time

/**
 * https://developer.android.com/develop/ui/views/notifications/live-update
 * https://developer.android.com/about/versions/16/features/progress-centric-notifications
 * https://github.com/android/platform-samples/tree/main/samples/user-interface/live-updates
 */

private const val notificationId: Int = NotificationsUtils.NOTIFICATION_ID_LIVE_UPDATE

object LiveUpdatesUtils {

    fun isSdkAvailable(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA

    fun upsert(liveData: LiveData) {
        val title: String = when (liveData) {
            is LiveData.Timer -> liveData.title
            is LiveData.Stopwatch -> liveData.title
        }

        val channel = NotificationsUtils.channelLiveUpdates()
        val manager = NotificationsUtils.manager

        val context = App.instance.applicationContext
        val pIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(App.instance, channel.id)
            .setSmallIcon(R.drawable.sf_timer_medium_semibold)
            .setOngoing(true)
            .setRequestPromotedOngoing(true)
            .setContentTitle(title)
            .setContentText(
                when (liveData) {
                    is LiveData.Timer ->
                        if (liveData.isFinished()) liveData.expiredString
                        else null
                    is LiveData.Stopwatch -> null
                }
            )
            .setShortCriticalText(
                when (liveData) {
                    // Show text if expired
                    is LiveData.Timer ->
                        // Docs: If less than 7 characters, show the whole text.
                        if (liveData.isFinished())
                            title.substring(0, title.length.coerceAtMost(7))
                        else null
                    // Always show timer
                    is LiveData.Stopwatch -> null
                }
            )
            .setWhen(
                when (liveData) {
                    is LiveData.Timer -> liveData.finishTime * 1_000L
                    is LiveData.Stopwatch -> liveData.startTime * 1_000L
                }
            )
            // Shows exact timer that updates every seconds
            .setUsesChronometer(true)
            .setChronometerCountDown(liveData is LiveData.Timer)
            // Before Android 16 progress replaces setContentText()
            .setStyle(
                when (liveData) {
                    is LiveData.Timer ->
                        if (liveData.isFinished() && isSdkAvailable())
                            NotificationCompat.ProgressStyle().setProgress(100)
                        else null
                    is LiveData.Stopwatch -> null
                }
            )
            .setContentIntent(pIntent)
            .build()
        manager.notify(notificationId, notification)
    }

    ///

    sealed class LiveData {

        companion object {

            fun build(liveActivity: LiveActivity): LiveData {
                return when (val timerType = liveActivity.timerType) {
                    is IntervalDb.TimerType.Timer -> Timer(
                        title = liveActivity.dynamicIslandTitle,
                        finishTime = timerType.finishTime,
                        expiredString = timerType.buildExpiredString(),
                    )
                    is IntervalDb.TimerType.Stopwatch -> Stopwatch(
                        title = liveActivity.dynamicIslandTitle,
                        startTime = timerType.startTime,
                    )
                }
            }
        }

        data class Timer(
            val title: String,
            val finishTime: Int,
            val expiredString: String,
        ) : LiveData() {

            fun isFinished(): Boolean =
                time() >= finishTime
        }

        class Stopwatch(
            val title: String,
            val startTime: Int,
        ) : LiveData()
    }
}
