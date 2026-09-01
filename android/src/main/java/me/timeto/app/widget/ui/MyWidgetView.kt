package me.timeto.app.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import me.timeto.app.toColor
import me.timeto.app.toGlanceColorProvider
import me.timeto.app.ui.c
import me.timeto.app.widget.MyWidgetOpenApp
import me.timeto.app.widget.ui.buttons.MyWidgetButtonsView
import me.timeto.app.widget.ui.home_bar.MyWidgetHomeBarView
import me.timeto.app.widget.ui.tasks.MyWidgetTasksView
import me.timeto.shared.vm.home.HomeMode
import me.timeto.shared.widget.WidgetChecklistUi
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
                .fillMaxWidth()
                .clickable(
                    MyWidgetOpenApp.buildAction(
                        context = LocalContext.current,
                        appAction = null,
                    )
                ),
            contentAlignment = Alignment.TopCenter,
        ) {

            Box(
                modifier = GlanceModifier
                    .padding(top = 8.dp)
                    .clickable {
                        widgetUi.timerStateUi.togglePomodoro()
                    },
                contentAlignment = Alignment.TopCenter,
            ) {

                Text(
                    text = widgetUi.timerStateUi.note,
                    style = TextStyle(
                        color = widgetUi.timerStateUi.noteColor.toColor().toGlanceColorProvider(),
                        fontSize = myWidgetPrimaryFontSize,
                        fontWeight = FontWeight.Medium,
                    ),
                )

                Text(
                    text = widgetUi.timerStateUi.timerText,
                    modifier = GlanceModifier
                        .padding(top = 12.dp),
                    style = TextStyle(
                        color = widgetUi.timerStateUi.timerColor.toColor().toGlanceColorProvider(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }

        Row(
            modifier = GlanceModifier
                .padding(start = myWidgetHPadding)
                .defaultWeight(),
        ) {
            val widgetChecklistUi: WidgetChecklistUi? =
                widgetUi.widgetChecklistUi
            if (widgetChecklistUi != null) {
                MyWidgetChecklistView(
                    widgetChecklistUi = widgetChecklistUi,
                    glanceModifier = GlanceModifier
                        .defaultWeight(),
                )
            }
            when (val homeMode = widgetUi.homeBarUi.homeMode) {
                is HomeMode.TaskFolder -> {
                    val homeTasksItemsUi = homeMode.homeTasksItemsUi
                    if (homeTasksItemsUi.isNotEmpty()) {
                        MyWidgetTasksView(
                            homeTasksItemsUi = homeTasksItemsUi,
                            glanceModifier = GlanceModifier
                                .defaultWeight(),
                        )
                    }
                }
                is HomeMode.NoteFolder -> {
                    // todo
                }
            }
        }

        MyWidgetHomeBarView(widgetUi.homeBarUi)

        MyWidgetButtonsView(widgetUi)
    }
}
