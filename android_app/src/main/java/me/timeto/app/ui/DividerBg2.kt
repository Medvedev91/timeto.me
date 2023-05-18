package me.timeto.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable

@Composable
fun DividerBg2(
    isVisible: Boolean = true,
) {
    val animateColor = animateColorAsState(
        if (isVisible) c.dividerBg2
        else c.transparent
    )
    Divider(color = animateColor.value)
}
