package me.timeto.app.ui.doc

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.SquircleShape
import me.timeto.app.ui.c

private val screenshotBorderColor: Color = c.gray5
private val screenshotSliderShape = SquircleShape(24.dp)

@Composable
fun DocScreenshotView(
    @DrawableRes resId: Int,
    fraction: Float = 1f,
    innerPadding: Dp = 0.dp,
) {
    Image(
        painter = painterResource(id = resId),
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxWidth(fraction = fraction)
            .clip(screenshotSliderShape)
            .border(1.dp, screenshotBorderColor, shape = screenshotSliderShape)
            .padding(innerPadding),
        contentDescription = "Screenshot",
        contentScale = ContentScale.Fit,
    )
}
