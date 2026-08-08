package me.timeto.app.widget.ui.home_bar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.size
import me.timeto.app.R
import me.timeto.app.toGlanceColorProvider
import me.timeto.app.widget.ui.symbol.MyWidgetSymbolView
import me.timeto.shared.TaskFolderUi

@Composable
fun MyWidgetHomeBarTaskFolderButton(
    taskFolderUi: TaskFolderUi,
    color: Color,
    glanceModifier: GlanceModifier,
    onClick: () -> Unit,
) {

    MyWidgetHomeBarIconButton(
        onClick = onClick,
        glanceModifier = glanceModifier,
    ) {
        if (taskFolderUi.taskFolderDb.isToday) {
            Image(
                provider = ImageProvider(R.drawable.ms_wb_sunny_fill),
                contentDescription = "Today",
                modifier = GlanceModifier
                    .size(myWidgetHomeBarIconSize),
                colorFilter = ColorFilter.tint(color.toGlanceColorProvider()),
            )
        } else if (taskFolderUi.taskFolderDb.isTomorrow) {
            Image(
                provider = ImageProvider(R.drawable.ms_dark_mode_fill),
                contentDescription = "Tomorrow",
                modifier = GlanceModifier
                    .size(myWidgetHomeBarIconSize),
                colorFilter = ColorFilter.tint(color.toGlanceColorProvider()),
            )
        } else {
            MyWidgetSymbolView(
                symbol = taskFolderUi.symbol,
                color = color,
                letterSize = myWidgetHomeBarLetterSize,
                iconSize = myWidgetHomeBarIconSize,
                emojiSize = myWidgetHomeBarLetterSize,
                glanceModifier = GlanceModifier,
            )
        }
    }
}
