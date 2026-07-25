package me.timeto.app.ui.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.H_PADDING
import me.timeto.app.MainActivity
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.SquircleShape

private val dialogShape = SquircleShape(24.dp)

@Composable
fun NavigationDialog(
    layer: NavigationLayer,
    innerPadding: PaddingValues = PaddingValues(H_PADDING),
    content: @Composable ColumnScope.(layer: NavigationLayer) -> Unit,
) {
    val mainActivity = LocalActivity.current as MainActivity
    val statusBarHeight: Dp =
        mainActivity.statusBarHeightFlow.collectAsState().value

    ZStack(
        modifier = Modifier
            .padding(top = statusBarHeight)
            .padding(vertical = 8.dp)
            .systemBarsPadding()
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        VStack(
            modifier = Modifier
                .padding(horizontal = H_PADDING * 2)
                .clip(dialogShape)
                .background(c.fg)
                .pointerInput(Unit) {}
                .padding(innerPadding)
        ) {
            content(layer)
        }
    }
}
