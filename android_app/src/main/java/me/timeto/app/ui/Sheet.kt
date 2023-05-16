package me.timeto.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.statusBarHeight
import me.timeto.shared.min

object Sheet {

    fun show(
        topPadding: Dp = 20.dp,
        content: @Composable (WrapperView.Layer) -> Unit,
    ) {
        WrapperView.Layer(
            enterAnimation = slideInVertically(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMedium,
                    visibilityThreshold = IntOffset.VisibilityThreshold
                ),
                initialOffsetY = { it }
            ),
            exitAnimation = slideOutVertically(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMedium,
                    visibilityThreshold = IntOffset.VisibilityThreshold
                ),
                targetOffsetY = { it }
            ),
            alignment = Alignment.BottomCenter,
            onClose = {},
            content = { layer ->
                Box(
                    /**
                     * Ordering is important. Otherwise, podding
                     * by height wouldn't work on close click.
                     */
                    modifier = Modifier
                        .padding(top = topPadding + statusBarHeight) // Restriction max height
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .pointerInput(Unit) { }
                ) {
                    content(layer)
                }
            }
        ).show()
    }

    @Composable
    fun HeaderView(
        onCancel: () -> Unit,
        title: String,
        doneText: String,
        isDoneEnabled: Boolean?, // null - hidden
        scrollToHeader: Int,
        maxLines: Int = Int.MAX_VALUE,
        onDone: () -> Unit,
    ) {

        val isLight = MaterialTheme.colors.isLight
        val bg = remember(isLight) { if (isLight) Color(0xFFF9F9F9) else Color(0xFF191919) }
        val bgDivider = remember(isLight) { if (isLight) Color(0xFFE9E9E9) else Color(0xFF1F1F1F) }

        val bgAlpha = (scrollToHeader.toFloat() / 50).min(1f)
        val bgAnimate = animateColorAsState(bg.copy(alpha = bgAlpha))
        val bgDividerAnimate = animateColorAsState(bgDivider.copy(alpha = bgAlpha))

        Box(
            modifier = Modifier.background(bgAnimate.value),
            contentAlignment = Alignment.BottomCenter // For divider
        ) {

            Box(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 16.dp),
            ) {

                Text(
                    "Cancel",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .clickable { onCancel() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = c.blue,
                    fontSize = 17.sp,
                )

                Text(
                    text = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 85.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W500,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = maxLines,
                    color = c.text,
                    textAlign = TextAlign.Center,
                )

                Text(
                    doneText,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .clickable(enabled = (isDoneEnabled == true)) {
                            onDone()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = animateColorAsState(
                        targetValue = when (isDoneEnabled) {
                            true -> c.blue
                            false -> c.textSecondary.copy(alpha = 0.4f)
                            null -> c.transparent
                        }
                    ).value,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W600
                )
            }

            Divider(
                color = bgDividerAnimate.value,
                thickness = 1.dp
            )
        }
    }
}
