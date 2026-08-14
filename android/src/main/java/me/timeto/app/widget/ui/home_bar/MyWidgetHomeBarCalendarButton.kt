package me.timeto.app.widget.ui.home_bar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.layout.size
import me.timeto.app.R
import me.timeto.app.toGlanceColorProvider

@Composable
fun MyWidgetHomeBarCalendarButton(
    color: Color,
    onClickAction: Action,
) {
    MyWidgetHomeBarIconButton(
        onClickAction = onClickAction,
        glanceModifier = GlanceModifier,
        content = {
            Image(
                provider = ImageProvider(R.drawable.sf_calendar_medium_light),
                contentDescription = "New Task",
                modifier = GlanceModifier
                    .size(18.dp),
                colorFilter = ColorFilter.tint(color.toGlanceColorProvider()),
            )
        },
    )
}
