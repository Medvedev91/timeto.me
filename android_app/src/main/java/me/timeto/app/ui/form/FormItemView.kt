package me.timeto.app.ui.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.ZStack
import me.timeto.app.c
import me.timeto.app.ui.Divider
import me.timeto.app.ui.SquircleShape

@Composable
fun FormItemView(
    isFirst: Boolean,
    isLast: Boolean,
    dividerPadding: PaddingValues = PaddingValues(start = H_PADDING),
    outerPadding: PaddingValues = PaddingValues(horizontal = H_PADDING),
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    ZStack(
        modifier = modifier
            .padding(outerPadding)
            .clip(SquircleShape(12.dp, angles = listOf(isFirst, isFirst, isLast, isLast))),
        contentAlignment = Alignment.TopCenter,
    ) {

        ZStack(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.fg),
            contentAlignment = Alignment.CenterStart,
        ) {
            content()
        }

        if (!isFirst) {
            Divider(Modifier.padding(dividerPadding))
        }
    }
}
