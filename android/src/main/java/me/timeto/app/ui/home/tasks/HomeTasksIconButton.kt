package me.timeto.app.ui.home.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.roundedShape

@Composable
fun HomeTasksIconButton(
    onClick: () -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    ZStack(
        modifier = modifier
            .size(HomeScreen__itemHeight)
            .clip(roundedShape)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
