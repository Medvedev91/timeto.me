package me.timeto.app

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import me.timeto.shared.LiveActivity
import me.timeto.shared.time

/**
 * https://developer.android.com/develop/ui/views/notifications/live-update
 * https://developer.android.com/about/versions/16/features/progress-centric-notifications
 * https://github.com/android/platform-samples/tree/main/samples/user-interface/live-updates
 */

private const val notificationId: Int = NotificationCenter.NOTIFICATION_ID_LIVE_UPDATE

object LiveUpdatesUtils {

    fun isSdkAvailable(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA

    fun close() {
        NotificationCenter.manager.cancel(notificationId)
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    fun upsert(liveData: LiveData) {
        val title = liveData.title
        val finishTime = liveData.finishTime

        val channel = NotificationCenter.channelLiveUpdates()
        val manager = NotificationCenter.manager

        val context = App.instance.applicationContext
        val pIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val isFinished: Boolean = time() >= finishTime
        val chipText: String = run {
            // Docs: If less than 7 characters, show the whole text.
            title.substring(0, title.length.coerceAtMost(7))
        }
        val notification = NotificationCompat.Builder(App.instance, channel.id)
            .setSmallIcon(R.drawable.sf_timer_medium_semibold)
            .setOngoing(true)
            .setRequestPromotedOngoing(true)
            .setContentTitle(title)
            .setContentText(if (isFinished) "${liveData.expiredString}  ⏰" else null)
            .setShortCriticalText(if (isFinished) chipText else null)
            .setWhen(finishTime * 1_000L)
            .setStyle(if (isFinished) NotificationCompat.ProgressStyle().setProgress(100) else null)
            .setContentIntent(pIntent)
            .build()
        manager.notify(notificationId, notification)
    }

    ///

    data class LiveData(
        val title: String,
        val finishTime: Int,
        val expiredString: String,
    ) {
        companion object {
            fun build(liveActivity: LiveActivity) = LiveData(
                title = liveActivity.dynamicIslandTitle,
                finishTime = liveActivity.intervalDb.id + liveActivity.intervalDb.timer,
                expiredString = liveActivity.intervalDb.getExpiredString(),
            )
        }
    }
}
