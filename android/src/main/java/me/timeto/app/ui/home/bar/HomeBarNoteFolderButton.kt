package me.timeto.app.ui.home.bar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.timeto.app.ui.symbol.SymbolView
import me.timeto.shared.Symbol

@Composable
fun HomeBarNoteFolderButton(
    symbol: Symbol,
    color: Color,
    onClick: () -> Unit,
) {
    HomeBarIconButton(
        onClick = onClick,
        modifier = Modifier,
    ) {
        SymbolView(
            symbol = symbol,
            color = color,
            letterSize = homeBarLetterSize,
            iconSize = homeBarIconSize,
            emojiSize = homeBarLetterSize,
            modifier = Modifier,
        )
    }
}
