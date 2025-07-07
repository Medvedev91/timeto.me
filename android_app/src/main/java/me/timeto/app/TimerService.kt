package me.timeto.app

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import me.timeto.shared.vm.home.HomeVm
import java.util.Timer

class TimerService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val state = HomeVm().state

        val serviceHandler = Handler(Looper.getMainLooper())
        serviceHandler.post(object: Runnable {
            override fun run() {
                val timerString = state.value.timerStateUi.timerText
                val note = state.value.timerStateUi.note
                val timerColor = state.value.timerStateUi.timerColor.toString()

                NotificationCenter.getManager().notify(60,
                if (timerColor == "red") buildTimerNotification(note, "—$timerString")
                else buildTimerNotification(note, timerString))

                serviceHandler.postDelayed(this, 1000)
            }
        })

        startForeground(buildTimerNotification("", ""))
        return START_STICKY
    }

    private fun startForeground(notification: Notification) {
        try {
            ServiceCompat.startForeground(
                this, 60, notification,
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else 0
            )
        } catch (e: Exception) {
            stopSelf()
        }

    }

    fun buildTimerNotification(title: String, text: String): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 60, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )3
        val channel = NotificationCenter.channelTimer()

        return NotificationCompat.Builder(this, channel.id)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.sf_timer_medium_thin)
            .setContentIntent(contentIntent).build()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


}
