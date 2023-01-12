package app.time_to.timeto.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import timeto.shared.min

@Composable
fun SheetHeaderView(
    onCancel: () -> Unit,
    title: String,
    doneText: String,
    isDoneEnabled: Boolean,
    scrollToHeader: Int,
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

        Row(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp)
        ) {

            Text(
                "Cancel",
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .clickable { onCancel() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = c.blue,
                fontSize = 17.sp,
            )

            SpacerW1()

            Text(
                title,
                fontSize = 22.sp,
                fontWeight = FontWeight.W500,
                color = c.text
            )

            SpacerW1()

            Text(
                doneText,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .clickable(enabled = isDoneEnabled) {
                        onDone()
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = animateColorAsState(targetValue = if (isDoneEnabled) c.blue else c.textSecondary.copy(alpha = 0.4f)).value,
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
