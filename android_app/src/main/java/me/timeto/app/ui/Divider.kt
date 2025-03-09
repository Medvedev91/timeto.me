package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.ZStack
import me.timeto.app.c
import me.timeto.app.onePx

@Composable
fun Divider(
    modifier: Modifier = Modifier,
) {
    ZStack(
        modifier = modifier
            .height(onePx)
            .fillMaxWidth()
            .background(c.divider),
    )
}
