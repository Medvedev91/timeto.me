package me.timeto.app.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.ZStack

@Composable
fun Padding(width: Dp = 0.dp, height: Dp = 0.dp) {
    ZStack(modifier = Modifier.width(width).height(height))
}
