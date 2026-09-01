package me.timeto.app.widget.ui.home_bar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import me.timeto.app.widget.ui.symbol.MyWidgetSymbolView
import me.timeto.shared.NoteFolderUi

@Composable
fun MyWidgetHomeBarNoteFolderButton(
    noteFolderUi: NoteFolderUi,
    color: Color,
    onClickAction: Action,
) {

    MyWidgetHomeBarIconButton(
        onClickAction = onClickAction,
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
