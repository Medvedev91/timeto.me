package me.timeto.app.widget

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import me.timeto.shared.ioScope
import me.timeto.shared.launchExIo
import me.timeto.shared.reportApi
import me.timeto.shared.widget.WidgetFlow
import kotlin.collections.forEach
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// todo https://developer.android.com/develop/ui/compose/glance/user-interaction#launch-service

// Внимание!
// Запускать сервис только из MyWidgetService.start().
class MyWidgetService : Service() {

    class ForegroundNotification(
        val notificationId: Int,
        val notification: Notification,
    )

    companion object {

        private var lastForegroundNotification: ForegroundNotification? = null

        fun start(
            context: Context,
            foregroundNotification: ForegroundNotification,
        ) {
            lastForegroundNotification = foregroundNotification
            launchExIo {
                if (getGlanceIds(context).isNotEmpty())
                    context.startForegroundService(buildServiceIntent(context))
            }
        }

        fun stop(context: Context) {
            context.stopService(buildServiceIntent(context))
        }

        private fun buildServiceIntent(context: Context): Intent {
            return Intent(context, MyWidgetService::class.java)
        }
    }

    ///

    private var timerJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val _this = this

        timerJob?.cancel()
        timerJob = ioScope().launch {

            try {

                val foregroundNotification: ForegroundNotification? =
                    lastForegroundNotification
                if (foregroundNotification == null) {
                    reportApi("MyWidgetService.onStartCommand() null foregroundNotification")
                    return@launch
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceCompat.startForeground(
                        _this,
                        foregroundNotification.notificationId,
                        foregroundNotification.notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                    )
                } else {
                    _this.startForeground(
                        foregroundNotification.notificationId,
                        foregroundNotification.notification,
                    )
                }

                val eachSecondFlow: Flow<Unit> = flow {
                    while (true) {
                        emit(Unit)
                        delay(1.seconds)
                    }
                }

                combine(
                    eachSecondFlow,
                    WidgetFlow.flow,
                ) { _, _ ->
                    val isWidgetsExists: Boolean =
                        updateWidgets()
                    if (!isWidgetsExists)
                        stopSelf()
                }.collect()
            } catch (e: Exception) {
                if (e is CancellationException)
                    println(";; MyWidgetService CancellationException")
                else
                    reportApi("MyWidgetService timerJob exception: $e")
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    ///

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun updateWidgets(): Boolean {
        val glanceIds: List<GlanceId> =
            getGlanceIds(this)
        if (glanceIds.isEmpty())
            return false

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(this, glanceId) {
                it[stringPreferencesKey("trigger")] = Uuid.random().toString()
            }
            MyWidget().update(this, glanceId)
        }

        return true
    }
}

private suspend fun getGlanceIds(context: Context): List<GlanceId> {
    return GlanceAppWidgetManager(context).getGlanceIds(MyWidget::class.java)
}
