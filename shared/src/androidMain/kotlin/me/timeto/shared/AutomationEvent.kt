package me.timeto.shared

import android.content.Intent

const val AUTOMATION_ACTION_TIMER_STARTED = "me.timeto.app.TIMER_STARTED"
const val AUTOMATION_ACTION_TIMER_EXPIRED = "me.timeto.app.TIMER_EXPIRED"
const val AUTOMATION_ACTION_STOPWATCH_STARTED = "me.timeto.app.STOPWATCH_STARTED"
const val AUTOMATION_EXTRA_ACTIVITY_NAME = "activity_name"
const val AUTOMATION_EXTRA_TIMER_SECONDS = "timer_seconds"

actual fun onTimerStarted(activityName: String, timerSeconds: Int) {
    androidApplication.sendBroadcast(
        Intent(AUTOMATION_ACTION_TIMER_STARTED).apply {
            putExtra(AUTOMATION_EXTRA_ACTIVITY_NAME, activityName)
            putExtra(AUTOMATION_EXTRA_TIMER_SECONDS, timerSeconds)
        }
    )
}

actual fun onStopwatchStarted(activityName: String) {
    androidApplication.sendBroadcast(
        Intent(AUTOMATION_ACTION_STOPWATCH_STARTED).apply {
            putExtra(AUTOMATION_EXTRA_ACTIVITY_NAME, activityName)
        }
    )
}
