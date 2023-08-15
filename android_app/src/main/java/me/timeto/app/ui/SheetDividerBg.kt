package me.timeto.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.c

@Composable
fun SheetDividerBg(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
) {
    Divider(c.sheetDividerBg, modifier, isVisible)
}
