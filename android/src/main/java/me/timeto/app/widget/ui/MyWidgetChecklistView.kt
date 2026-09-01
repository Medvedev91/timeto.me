package me.timeto.app.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import me.timeto.app.Haptic
import me.timeto.app.R
import me.timeto.app.toGlanceColorProvider
import me.timeto.app.ui.c
import me.timeto.shared.widget.WidgetChecklistUi

@Composable
fun MyWidgetChecklistView(
    widgetChecklistUi: WidgetChecklistUi,
    glanceModifier: GlanceModifier,
) {

    LazyColumn(
        modifier = glanceModifier,
    ) {

        widgetChecklistUi.itemsUi.forEach { itemUi ->

            item {

                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable {
                            itemUi.toggle()
                            Haptic.shot()
                        }
                        .height(myWidgetItemHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    ChecklistIconView(
                        iconType =
                            if (itemUi.itemDb.isChecked)
                                ChecklistIconType.checked
                            else
                                ChecklistIconType.unchecked,
                    )

                    Text(
                        text = itemUi.text,
                        style = TextStyle(
                            color = c.white.toGlanceColorProvider(),
                            fontSize = myWidgetPrimaryFontSize,
                        ),
                        modifier = GlanceModifier
                            .padding(start = 7.dp),
                        maxLines = 1,
                    )

                    // todo
                    // TriggersIconsView(
                    //    checklistsDb = itemUi.textFeatures.checklistsDb,
                    //    shortcutsDb = itemUi.textFeatures.shortcutsDb,
                    // )
                }
            }
        }
    }
}

private enum class ChecklistIconType {
    checked, unchecked
}

@Composable
private fun ChecklistIconView(
    iconType: ChecklistIconType,
) {
    val isFilled = iconType != ChecklistIconType.unchecked
    Box(
        modifier = GlanceModifier
            .size(myWidgetItemCircleHeight)
            .cornerRadius(99.dp)
            .background(if (!isFilled) c.homeFg else c.white),
        contentAlignment = Alignment.Center,
    ) {

        if (!isFilled) {
            Box(
                modifier = GlanceModifier
                    .size(myWidgetItemCircleHeight - 4.dp)
                    .cornerRadius(99.dp)
                    .background(c.black),
            ) {}
        }

        if (iconType == ChecklistIconType.checked) {
            Image(
                provider = ImageProvider(R.drawable.sf_checkmark_medium_semibold),
                contentDescription = "Checkbox",
                modifier = GlanceModifier
                    .size(9.dp),
                colorFilter = ColorFilter.tint(c.black.toGlanceColorProvider()),
            )
        }
    }
}
