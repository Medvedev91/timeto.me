package me.timeto.app.ui.symbol

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import me.timeto.shared.Symbol

@Composable
fun SymbolView(
    symbol: Symbol,
    color: Color,
    letterSize: TextUnit,
    iconSize: Dp,
    emojiSize: TextUnit,
    modifier: Modifier,
) {
    when (symbol) {
        is Symbol.Letter -> {
            SymbolLetterView(
                letter = symbol,
                color = color,
                size = letterSize,
                modifier = modifier,
            )
        }
        is Symbol.Icon -> {
            SymbolIconView(
                icon = symbol,
                color = color,
                size = iconSize,
                modifier = modifier,
            )
        }
        is Symbol.Emoji -> {
            SymbolEmojiView(
                emoji = symbol,
                size = emojiSize,
                modifier = modifier,
            )
        }
    }
}
