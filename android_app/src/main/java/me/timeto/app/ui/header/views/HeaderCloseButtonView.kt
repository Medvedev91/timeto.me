package me.timeto.app.ui.header.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.timeto.app.R
import me.timeto.app.ZStack
import me.timeto.app.c
import me.timeto.app.roundedShape

@Composable
fun HeaderCloseButtonView(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ZStack(
        modifier = modifier
            .size(31.dp)
            .clip(roundedShape)
            .background(c.fg)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.sf_xmark_small_medium),
            contentDescription = "Close",
            tint = c.tertiaryText,
            modifier = Modifier
                .size(11.dp),
        )
    }
}
