package me.timeto.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.timeto.app.R

@Composable
fun TextFieldClearButtonView(
    text: String,
    onClick: () -> Unit,
) {
    Row {
        AnimatedVisibility(
            visible = text.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Icon(
                painterResource(id = R.drawable.sf_xmark_circle_fill_medium_medium),
                "Clear",
                tint = c.textSecondary.copy(alpha = 0.3f),
                modifier = Modifier
                    .padding(end = 2.dp)
                    .size(32.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .clickable {
                        onClick()
                    }
                    .padding(8.dp)
            )
        }
    }
}
