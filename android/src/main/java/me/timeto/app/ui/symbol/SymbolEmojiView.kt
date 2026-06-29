package me.timeto.app.ui.symbol

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import me.timeto.shared.Symbol

@Composable
fun SymbolEmojiView(
    emoji: Symbol.Emoji,
    size: TextUnit,
    modifier: Modifier,
) {
    Text(
        text = emoji.emoji,
        modifier = modifier,
        fontWeight = FontWeight.SemiBold,
        fontSize = size,
    )
}
