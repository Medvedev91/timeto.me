package me.timeto.app.widget.ui.symbol

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.glance.GlanceModifier
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import me.timeto.shared.Symbol

@Composable
fun MyWidgetSymbolEmojiView(
    emoji: Symbol.Emoji,
    size: TextUnit,
    glanceModifier: GlanceModifier,
) {
    Text(
        text = emoji.emoji,
        modifier = glanceModifier,
        style = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = size,
        ),
    )
}
