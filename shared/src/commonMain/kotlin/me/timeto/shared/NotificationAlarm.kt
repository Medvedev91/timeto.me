package me.timeto.shared

import kotlinx.coroutines.flow.MutableSharedFlow
import me.timeto.shared.db.IntervalDb

data class NotificationAlarm(
    val title: String,
    val text: String,
    val inSeconds: Int,
    val type: Type,
    val liveActivity: LiveActivity,
) {

    companion object {

        const val NO_ACTIVITY_DAYS_LIMIT = 7

        // Not StateFlow to reschedule same data object
        val flow = MutableSharedFlow<List<NotificationAlarm>>()

        suspend fun rescheduleAll() {
            rescheduleNotifications()
        }
    }

    ///

    sealed class Type {
        object TimeToBreak : Type()
        object Overdue : Type()
        data class NoActivity(val day: Int) : Type()
    }
}

private suspend fun rescheduleNotifications() {
    val lastIntervalDb = IntervalDb.selectLastOneOrNull()!!

    val liveActivity = LiveActivity(lastIntervalDb)
    LiveActivity.flow.emit(liveActivity)

    val notifications = mutableListOf<NotificationAlarm>()

    val inSeconds: Int = lastIntervalDb.finishTime - time()
    if (inSeconds > 0) {
        notifications.add(
            NotificationAlarm(
                title = "Time Is Over ⏰",
                text = lastIntervalDb.getExpiredString(),
                inSeconds = inSeconds,
                type = NotificationAlarm.Type.TimeToBreak,
                liveActivity = liveActivity,
            ),
        )
    }

    val oneDaySeconds = 86_400
    (1..NotificationAlarm.NO_ACTIVITY_DAYS_LIMIT).forEach { day ->
        val notificationTime: Int =
            lastIntervalDb.id + (day * oneDaySeconds)
        val inSeconds: Int =
            notificationTime - time()
        if (inSeconds <= 0)
            return@forEach
        notifications.add(
            NotificationAlarm(
                title = "No activity for $day day${if (day > 1) "s" else ""}",
                text = "It's okay. Just back to goals.",
                inSeconds = inSeconds,
                type = NotificationAlarm.Type.NoActivity(day = day),
                liveActivity = liveActivity,
            ),
        )
    }

    NotificationAlarm.flow.emit(notifications)

    /*
    val activityDb = lastInterval.getActivity()
    val pomodoroTimer = activityDb.pomodoro_timer
    if (pomodoroTimer > 0) {
        scheduledNotificationsDataFlow.emit(
            listOf(
                ScheduledNotificationData(
                    title = "Time to Break  ✅",
                    text = if (totalMinutes == 1) "1 minute has expired" else "$totalMinutes minutes have expired",
                    inSeconds = inSeconds,
                    type = ScheduledNotificationData.TYPE.BREAK,
                ),
                ScheduledNotificationData(
                    title = "Break Is Over ⏰",
                    text = "Restart or set the timer",
                    inSeconds = inSeconds + pomodoroTimer,
                    type = ScheduledNotificationData.TYPE.OVERDUE,
                ),
            )
        )
    } else {
        scheduledNotificationsDataFlow.emit(
            listOf(
                ScheduledNotificationData(
                    title = "Time Is Over ⏰",
                    text = if (totalMinutes == 1) "1 minute has expired" else "$totalMinutes minutes have expired",
                    inSeconds = inSeconds,
                    type = ScheduledNotificationData.TYPE.OVERDUE,
                ),
            )
        )
    }
    */
}
