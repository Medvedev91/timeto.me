package me.timeto.app.widget.ui.symbol

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.glance.GlanceModifier
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import me.timeto.app.toGlanceColorProvider
import me.timeto.shared.Symbol

@Composable
fun MyWidgetSymbolLetterView(
    letter: Symbol.Letter,
    color: Color,
    size: TextUnit,
    glanceModifier: GlanceModifier,
) {
    Text(
        text = letter.letter,
        modifier = glanceModifier,
        style = TextStyle(
            fontWeight = FontWeight.Bold,
            color = color.toGlanceColorProvider(),
            fontSize = size,
        ),
    )
}
