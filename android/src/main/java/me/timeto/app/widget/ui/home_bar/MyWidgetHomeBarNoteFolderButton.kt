package me.timeto.app.widget.ui.home_bar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import me.timeto.app.widget.ui.symbol.MyWidgetSymbolView
import me.timeto.shared.NoteFolderUi

@Composable
fun MyWidgetHomeBarNoteFolderButton(
    noteFolderUi: NoteFolderUi,
    color: Color,
    onClick: () -> Unit,
) {

    MyWidgetHomeBarIconButton(
        onClick = onClick,
        glanceModifier = GlanceModifier,
    ) {
        MyWidgetSymbolView(
            symbol = noteFolderUi.symbol,
            color = color,
            letterSize = myWidgetHomeBarLetterSize,
            iconSize = myWidgetHomeBarIconSize,
            emojiSize = myWidgetHomeBarLetterSize,
            glanceModifier = GlanceModifier,
        )
    }
}
