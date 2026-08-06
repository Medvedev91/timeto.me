package me.timeto.app.widget.ui.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import me.timeto.app.widget.ui.myWidgetHPadding
import me.timeto.app.widget.widgetButtonsRowHeight
import me.timeto.shared.vm.home.buttons.HomeButtonType
import me.timeto.shared.widget.WidgetUi

@Composable
fun MyWidgetButtonsView(
    widgetUi: WidgetUi,
) {

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = myWidgetHPadding),
    ) {

        widgetUi.buttonsTodo.forEach { buttonUi ->
            // Many Box or not working. WTF!?
            Box(
                modifier = GlanceModifier
                    .padding(start = buttonUi.offsetX.dp, top = buttonUi.offsetY.dp),
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(width = buttonUi.width.dp, height = widgetButtonsRowHeight),
                ) {
                    when (val type = buttonUi.type) {
                        is HomeButtonType.Activity ->
                            MyWidgetButtonActivityView(
                                width = buttonUi.width.dp,
                                activity = type,
                            )
                    }
                }
            }
        }
    }
}
