package me.timeto.app.widget.ui.symbol

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.size
import me.timeto.app.toGlanceColorProvider
import me.timeto.app.ui.symbol.symbolIconResId
import me.timeto.shared.Symbol

@Composable
fun MyWidgetSymbolIconView(
    icon: Symbol.Icon,
    color: Color,
    size: Dp,
    glanceModifier: GlanceModifier,
) {
    Image(
        provider = ImageProvider(icon.symbolIconResId()),
        contentDescription = icon.iconEnum.name,
        modifier = glanceModifier
            .size(size),
        colorFilter = ColorFilter.tint(color.toGlanceColorProvider()),
    )
}
