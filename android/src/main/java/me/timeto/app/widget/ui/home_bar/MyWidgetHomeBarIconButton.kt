package me.timeto.app.widget.ui.home_bar

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.size
import me.timeto.app.widget.ui.myWidgetItemHeight

@Composable
fun MyWidgetHomeBarIconButton(
    onClickAction: Action,
    glanceModifier: GlanceModifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = glanceModifier
            .size(myWidgetItemHeight)
            .cornerRadius(99.dp)
            .clickable(onClickAction),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
