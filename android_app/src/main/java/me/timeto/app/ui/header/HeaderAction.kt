package me.timeto.app.ui.header

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.HStack
import me.timeto.app.H_PADDING
import me.timeto.app.H_PADDING_HALF
import me.timeto.app.VStack
import me.timeto.app.c
import me.timeto.app.halfDpCeil
import me.timeto.app.roundedShape
import me.timeto.app.ui.header.views.HeaderTitleView
import me.timeto.app.ui.header.views.HeaderView

@Composable
fun HeaderAction(
    title: String,
    actionText: String,
    isEnabled: Boolean,
    scrollState: ScrollableState?,
    onCancel: () -> Unit,
    onDone: () -> Unit,
) {

    HeaderView(
        scrollState = scrollState,
    ) {

        VStack(
            modifier = Modifier
                .padding(start = halfDpCeil),
        ) {

            Text(
                text = "Cancel",
                modifier = Modifier
                    .offset(y = 1.dp)
                    .padding(start = H_PADDING_HALF, top = 12.dp)
                    .clip(roundedShape)
                    .clickable { onCancel() }
                    .padding(horizontal = H_PADDING_HALF),
                color = c.textSecondary,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
            )

            HStack(
                modifier = Modifier
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                HeaderTitleView(
                    title = title,
                )

                Text(
                    text = actionText,
                    modifier = Modifier
                        .padding(end = H_PADDING)
                        .clip(roundedShape)
                        .background(if (isEnabled) c.blue else Color.DarkGray)
                        .clickable(isEnabled) {
                            onDone()
                        }
                        .padding(
                            horizontal = 10.dp,
                            vertical = 3.dp,
                        ),
                    color = if (isEnabled) c.text else c.textSecondary,
                    fontSize = Header__buttonFontSize,
                    fontWeight = Header__titleFontWeight,
                )
            }
        }
    }
}
