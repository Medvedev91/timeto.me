package me.timeto.app.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Padding(
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp
) {
    ZStack(
        modifier = Modifier
            .width(horizontal)
            .height(vertical)
    )
}
