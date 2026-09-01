package me.timeto.app.widget

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import me.timeto.app.widget.ui.MyWidgetOpenAppView
import me.timeto.app.widget.ui.MyWidgetView
import me.timeto.app.widget.ui.buttons.myWidgetButtonsSpacing
import me.timeto.app.widget.ui.myWidgetHPadding
import me.timeto.app.widget.ui.myWidgetItemHeight
import me.timeto.shared.widget.WidgetUi

class MyWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    companion object {
        // Иногда по непонятным причинам View загружается заново,
        // соответственно на момент загрузки отображается пустой экран.
        // На этот случай кешируем последнее состояние.
        private var lastWidgetUi: WidgetUi? = null
    }

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {

        provideContent {

            val prefs: Preferences =
                currentState<Preferences>()

            val widgetUiState: MutableState<WidgetUi?> = remember {
                mutableStateOf(lastWidgetUi)
            }
            val widgetUi: WidgetUi? =
                widgetUiState.value

            val size: DpSize =
                LocalSize.current
            val trigger: String? =
                prefs[stringPreferencesKey("trigger")]
            LaunchedEffect(size, trigger) {
                val widgetUi = WidgetUi.build(
                    width = size.width.value - (myWidgetHPadding.value * 2),
                    rowHeight = myWidgetItemHeight.value,
                    spacing = myWidgetButtonsSpacing.value,
                )
                lastWidgetUi = widgetUi
                widgetUiState.value = widgetUi
            }

            if (widgetUi != null) {
                MyWidgetView(widgetUi)
            } else {
                MyWidgetOpenAppView()
            }
        }
    }
}
