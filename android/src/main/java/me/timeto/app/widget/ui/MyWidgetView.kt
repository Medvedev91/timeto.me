package me.timeto.app.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import me.timeto.app.toColor
import me.timeto.app.toGlanceColorProvider
import me.timeto.app.ui.c
import me.timeto.app.widget.ui.buttons.MyWidgetButtonsView
import me.timeto.shared.widget.WidgetUi

@Composable
fun MyWidgetView(
    widgetUi: WidgetUi,
) {

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(bottom = 8.dp)
            .background(c.black),
    ) {

        Box(
            modifier = GlanceModifier
                .padding(start = myWidgetHPadding, top = 12.dp)
                .defaultWeight(),
        ) {

            Text(
                text = widgetUi.timerNote,
                style = TextStyle(
                    color = widgetUi.timerNoteColorEnum.toColor().toGlanceColorProvider(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )

            Text(
                text = widgetUi.timerText,
                modifier = GlanceModifier
                    .padding(top = 20.dp),
                style = TextStyle(
                    color = widgetUi.timerTextColorEnum.toColor().toGlanceColorProvider(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }

        MyWidgetButtonsView(widgetUi)
    }
}
