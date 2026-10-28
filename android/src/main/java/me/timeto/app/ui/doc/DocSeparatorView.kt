package me.timeto.app.ui.doc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c

@Composable
fun DocSeparatorView() {
    ZStack(
        modifier = Modifier
            .padding(horizontal = H_PADDING, vertical = 20.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(c.divider),
    )
}
