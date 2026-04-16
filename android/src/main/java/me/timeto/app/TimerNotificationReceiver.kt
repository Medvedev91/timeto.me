package me.timeto.app

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import me.timeto.shared.LiveActivity
import me.timeto.shared.launchExIo
import me.timeto.shared.reportApi

class TimerNotificationReceiver : BroadcastReceiver() {

    companion object {

        const val EXTRA_TITLE = "title"
        const val EXTRA_TEXT = "text"
        const val EXTRA_REQUEST_CODE = "request_code"

        // region EXTRA_LIVE
        private const val EXTRA_LIVE_TITLE = "live_title"
        private const val EXTRA_LIVE_TIME = "live_time"
        private const val EXTRA_LIVE_IS_TIMER_OR_STOPWATCH = "live_is_timer_or_stopwatch"
        private const val EXTRA_LIVE_EXPIRED_STRING = "live_expired_string"
        // endregion

        //
        // Live Data Encoding

        fun liveDataEncode(
            intent: Intent,
            liveActivity: LiveActivity,
        ) {
            val liveData = LiveUpdatesUtils.LiveData.build(liveActivity)
            intent.putExtra(
                EXTRA_LIVE_TITLE,
                when (liveData) {
                    is LiveUpdatesUtils.LiveData.Timer -> liveData.title
                    is LiveUpdatesUtils.LiveData.Stopwatch -> liveData.title
                }
            )
            intent.putExtra(
                EXTRA_LIVE_TIME,
                when (liveData) {
                    is LiveUpdatesUtils.LiveData.Timer -> liveData.finishTime
                    is LiveUpdatesUtils.LiveData.Stopwatch -> liveData.startTime
                }
            )
            intent.putExtra(
                EXTRA_LIVE_IS_TIMER_OR_STOPWATCH,
                when (liveData) {
                    is LiveUpdatesUtils.LiveData.Timer -> true
                    is LiveUpdatesUtils.LiveData.Stopwatch -> false
                }
            )
            if (liveData is LiveUpdatesUtils.LiveData.Timer)
                intent.putExtra(EXTRA_LIVE_EXPIRED_STRING, liveData.expiredString)
        }

        fun liveDataDecodeOrNull(
            intent: Intent,
        ): LiveUpdatesUtils.LiveData? {

            val liveTitle: String =
                intent.getStringExtra(EXTRA_LIVE_TITLE) ?: return null
            val liveTime: Int =
                intent.getIntExtra(EXTRA_LIVE_TIME, 0).takeIf { it > 0 } ?: return null
            val liveIsCountUpOrDown: Boolean =
                intent.getBooleanExtra(EXTRA_LIVE_IS_TIMER_OR_STOPWATCH, true)

            if (liveIsCountUpOrDown) {
                return LiveUpdatesUtils.LiveData.Stopwatch(
                    title = liveTitle,
                    startTime = liveTime,
                )
            }

            val liveExpiredString: String =
                intent.getStringExtra(EXTRA_LIVE_EXPIRED_STRING) ?: return null
            return LiveUpdatesUtils.LiveData.Timer(
                title = liveTitle,
                finishTime = liveTime,
                expiredString = liveExpiredString,
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val manager = NotificationsUtils.manager

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

            NotificationsUtils.NOTIFICATION_ID_BREAK -> {
                Triple(
                    R.drawable.readme_notification_timer_checkmark,
                    0x34C759,
                    NotificationsUtils.channelTimerExpired(),
                )
            }

            NotificationsUtils.NOTIFICATION_ID_OVERDUE -> {
                Triple(
                    R.drawable.readme_notification_alarm,
                    0x0055FF,
                    NotificationsUtils.channelTimerOverdue(),
                )
            }

            in NotificationsUtils.NOTIFICATION_ID_NO_ACTIVITY_RANGE -> {
                Triple(
                    R.drawable.readme_notification_alarm,
                    0x0055FF,
                    NotificationsUtils.channelTimerOverdue(),
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

            val liveData = liveDataDecodeOrNull(intent) ?: return@launchExIo
            LiveUpdatesUtils.upsert(liveData)

            if (requestCode == NotificationsUtils.NOTIFICATION_ID_BREAK) {
                // Await to play sound and close notification
                try {
                    delay(3_000)
                } catch (_: CancellationException) {
                }
                manager.cancel(requestCode)
            }
        }
    }
}
