package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import me.timeto.app.VStack
import me.timeto.app.c

@Composable
fun Screen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    VStack(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
            .pointerInput(Unit) {},
    ) {
        content()
    }
}
