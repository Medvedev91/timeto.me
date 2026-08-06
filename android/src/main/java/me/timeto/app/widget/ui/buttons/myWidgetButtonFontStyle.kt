package me.timeto.app.widget.ui.buttons

import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import me.timeto.app.toGlanceColorProvider
import me.timeto.app.ui.c

val myWidgetButtonFontStyle = TextStyle(
    color = c.white.toGlanceColorProvider(),
    fontSize = myWidgetButtonFontSize,
    fontWeight = FontWeight.Medium,
)
