package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Divider(
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    ZStack(
        modifier = modifier
            .height(onePx)
            .fillMaxWidth()
            .background(color ?: c.divider),
    )
}
