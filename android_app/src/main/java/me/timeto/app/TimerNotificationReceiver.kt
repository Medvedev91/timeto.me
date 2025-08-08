package me.timeto.app

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.delay
import me.timeto.shared.launchExIo
import me.timeto.shared.reportApi

class TimerNotificationReceiver : BroadcastReceiver() {

    companion object {

        const val EXTRA_TITLE = "title"
        const val EXTRA_TEXT = "text"
        const val EXTRA_REQUEST_CODE = "request_code"
        const val EXTRA_LIVE_TITLE = "live_title"
        const val EXTRA_LIVE_FINISH_TIME = "live_finish_time"
        const val EXTRA_LIVE_EXPIRED_STRING = "live_expired_string"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val manager = NotificationCenter.manager

        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0)

        val pIntent = PendingIntent.getActivity(
            context,
            requestCode,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        /**
         * WARNING
         * Do not forget about channelTimerExpired()/channelTimerOverdue()
         */
        val (iconId, color, channel) = when (requestCode) {

            NotificationCenter.NOTIFICATION_ID_BREAK -> {
                Triple(
                    R.drawable.readme_notification_timer_checkmark,
                    0x34C759,
                    NotificationCenter.channelTimerExpired(),
                )
            }

            NotificationCenter.NOTIFICATION_ID_OVERDUE -> {
                Triple(
                    R.drawable.readme_notification_alarm,
                    0x0055FF,
                    NotificationCenter.channelTimerOverdue(),
                )
            }

            else -> {
                reportApi("TimerNotificationReceiver invalid request code $requestCode")
                throw Exception()
            }
        }

        val notification = NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(iconId)
            .setColor(color)
            .setContentTitle(intent.getStringExtra(EXTRA_TITLE))
            .setContentText(intent.getStringExtra(EXTRA_TEXT))
            .setContentIntent(pIntent)
            .build()

        manager.notify(requestCode, notification)

        // No matter if Live Updates disabled in app settings,
        // it will look like normal push replacement.
        launchExIo {
            if (LiveUpdatesUtils.isSdkAvailable() && !manager.canPostPromotedNotifications())
                return@launchExIo

            val liveTitle: String =
                intent.getStringExtra(EXTRA_LIVE_TITLE) ?: return@launchExIo
            val liveFinishTime: Int =
                intent.getIntExtra(EXTRA_LIVE_FINISH_TIME, 0).takeIf { it > 0 } ?: return@launchExIo
            val liveExpiredString: String =
                intent.getStringExtra(EXTRA_LIVE_EXPIRED_STRING) ?: return@launchExIo
            LiveUpdatesUtils.upsert(
                LiveUpdatesUtils.LiveData(
                    title = liveTitle,
                    finishTime = liveFinishTime,
                    expiredString = liveExpiredString,
                )
            )
            // Await to play sound and close notification
            delay(3_000)
            manager.cancel(requestCode)
        }
    }
}
