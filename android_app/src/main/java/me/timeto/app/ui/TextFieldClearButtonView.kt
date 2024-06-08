package me.timeto.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.timeto.app.R
import me.timeto.app.ZStack
import me.timeto.app.c
import me.timeto.app.roundedShape

@Composable
fun TextFieldClearButtonView(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ZStack(
        modifier = modifier,
    ) {
        AnimatedVisibility(
            visible = text.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Icon(
                painterResource(id = R.drawable.sf_xmark_circle_fill_medium_medium),
                "Clear",
                tint = c.tertiaryText,
                modifier = Modifier
                    .size(32.dp)
                    .clip(roundedShape)
                    .clickable {
                        onClick()
                    }
                    .padding(8.dp),
            )
        }
    }
}
