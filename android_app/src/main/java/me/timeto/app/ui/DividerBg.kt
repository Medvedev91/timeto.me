package me.timeto.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.c

@Composable
fun DividerBg(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
) {
    Divider(c.dividerBg, modifier, isVisible)
}
