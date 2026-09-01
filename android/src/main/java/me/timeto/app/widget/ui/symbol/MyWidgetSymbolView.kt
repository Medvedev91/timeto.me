package me.timeto.app.widget.ui.symbol

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.glance.GlanceModifier
import me.timeto.shared.Symbol

@Composable
fun MyWidgetSymbolView(
    symbol: Symbol,
    color: Color,
    letterSize: TextUnit,
    iconSize: Dp,
    emojiSize: TextUnit,
    glanceModifier: GlanceModifier,
) {
    when (symbol) {
        is Symbol.Letter -> {
            MyWidgetSymbolLetterView(
                letter = symbol,
                color = color,
                size = letterSize,
                glanceModifier = glanceModifier,
            )
        }
        is Symbol.Icon -> {
            MyWidgetSymbolIconView(
                icon = symbol,
                color = color,
                size = iconSize,
                glanceModifier = glanceModifier,
            )
        }
        is Symbol.Emoji -> {
            MyWidgetSymbolEmojiView(
                emoji = symbol,
                size = emojiSize,
                glanceModifier = glanceModifier,
            )
        }
    }
}
