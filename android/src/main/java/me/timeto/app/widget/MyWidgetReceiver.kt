package me.timeto.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class MyWidgetReceiver : GlanceAppWidgetReceiver() {

    companion object {
        // Для обновления виджета нужен запуск фонового сервиса.
        // Мы не можем запустить сервис из фона, по этому
        // устанавливаем флаг тут и обрабатываем в приложении.
        var needRestart = false
    }

    override val glanceAppWidget = MyWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        needRestart = true
    }

    override fun onDisabled(context: Context) {
        super.onEnabled(context)
        MyWidgetService.stop(context)
    }
}
