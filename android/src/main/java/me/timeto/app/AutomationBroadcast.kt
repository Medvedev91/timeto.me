package me.timeto.app

import android.content.Context
import android.content.Intent
import me.timeto.shared.AUTOMATION_ACTION_TIMER_EXPIRED
import me.timeto.shared.AUTOMATION_EXTRA_ACTIVITY_NAME
import me.timeto.shared.AUTOMATION_EXTRA_TIMER_SECONDS

object AutomationBroadcast {

    fun sendTimerExpired(context: Context, activityName: String, timerSeconds: Int) {
        context.sendBroadcast(
            Intent(AUTOMATION_ACTION_TIMER_EXPIRED).apply {
                putExtra(AUTOMATION_EXTRA_ACTIVITY_NAME, activityName)
                putExtra(AUTOMATION_EXTRA_TIMER_SECONDS, timerSeconds)
            }
        )
    }
}
