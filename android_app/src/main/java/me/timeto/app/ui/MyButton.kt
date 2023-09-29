package me.timeto.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.c
import me.timeto.app.squircleShape

@Composable
fun MyButton(
    text: String,
    isEnabled: Boolean,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    extraPaddings: Pair<Int, Int> = 0 to 0, // horizontal, vertical
    fontSize: TextUnit = 15.sp,
    clip: Shape = squircleShape,
    onClick: () -> Unit
) {
    val bg = animateColorAsState(if (isEnabled) backgroundColor else Color.LightGray)

    Text(
        text,
        modifier
            .clip(clip)
            .background(bg.value)
            .clickable(enabled = isEnabled) {
                onClick()
            }
            .padding(
                horizontal = (16 + extraPaddings.first).dp,
                vertical = (8 + extraPaddings.second).dp
            ),
        color = c.white,
        fontWeight = FontWeight.W500,
        fontSize = fontSize,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}