package me.timeto.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.c

@Composable
fun SheetDividerFg(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
) {
    Divider(c.sheetDividerFg, modifier, isVisible)
}
