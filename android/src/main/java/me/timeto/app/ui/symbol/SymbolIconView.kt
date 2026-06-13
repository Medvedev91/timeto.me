package me.timeto.app.ui.symbol

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import me.timeto.shared.Symbol

@Composable
fun SymbolIconView(
    icon: Symbol.Icon,
    color: Color,
    size: Dp,
    modifier: Modifier,
) {
    Icon(
        painter = painterResource(id = icon.iconResId()),
        contentDescription = icon.iconEnum.name,
        modifier = modifier
            .size(size),
        tint = color,
    )
}
