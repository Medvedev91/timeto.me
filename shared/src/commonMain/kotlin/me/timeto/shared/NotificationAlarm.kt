package me.timeto.shared

import kotlinx.coroutines.flow.MutableSharedFlow
import me.timeto.shared.db.IntervalDb

data class NotificationAlarm(
    val title: String,
    val text: String,
    val inSeconds: Int,
    val type: Type,
) {

    companion object {

        val flow = MutableSharedFlow<List<NotificationAlarm>>()

        suspend fun rescheduleAll() {
            rescheduleNotifications()
        }
    }

    ///

    enum class Type {
        timeToBreak, overdue,
    }
}

private suspend fun rescheduleNotifications() {
    val lastInterval: IntervalDb = IntervalDb.selectLastOneOrNull()!!
    val inSeconds: Int = (lastInterval.id + lastInterval.timer) - time()
    if (inSeconds <= 0)
        return

    val totalMinutes: Int = lastInterval.timer / 60
    NotificationAlarm.flow.emit(
        listOf(
            NotificationAlarm(
                title = "Time Is Over ⏰",
                text =
                    if (totalMinutes == 1) "1 minute has expired"
                    else "$totalMinutes minutes have expired",
                inSeconds = inSeconds,
                type = NotificationAlarm.Type.timeToBreak,
            ),
        )
    )

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
