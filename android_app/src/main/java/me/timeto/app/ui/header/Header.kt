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
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.H_PADDING_HALF
import me.timeto.app.ui.VStack
import me.timeto.app.c
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.header.views.HeaderView

val Header__titleFontSize = 26.sp // Golden ratio to list's text
val Header__titleFontWeight = FontWeight.ExtraBold

val Header__buttonFontSize = 15.sp

@Composable
fun Header(
    title: String,
    scrollState: ScrollableState?,
    actionButton: HeaderActionButton?,
    cancelButton: HeaderCancelButton?,
    secondaryButtons: List<HeaderSecondaryButton> = listOf(),
) {

    HeaderView(
        scrollState = scrollState,
    ) {

        VStack {

            if (cancelButton != null || secondaryButtons.isNotEmpty()) {
                HStack(
                    modifier = Modifier
                        .offset(y = 1.dp)
                        .padding(horizontal = H_PADDING_HALF)
                        .padding(top = 12.dp),
                ) {
                    if (cancelButton != null) {
                        TopButtonView(
                            text = cancelButton.text,
                            color = c.textSecondary,
                            onClick = cancelButton.onClick,
                        )
                    }
                    SpacerW1()
                    secondaryButtons.forEach { secondaryButton ->
                        TopButtonView(
                            text = secondaryButton.text,
                            color = c.blue,
                            onClick = secondaryButton.onClick,
                        )
                    }
                }
            }

            HStack(
                modifier = Modifier
                    .padding(
                        top = if (cancelButton != null) 0.dp else 20.dp,
                        bottom = 6.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Text(
                    text = title,
                    modifier = Modifier
                        .padding(start = H_PADDING)
                        .weight(1f),
                    fontSize = Header__titleFontSize,
                    fontWeight = Header__titleFontWeight,
                    color = c.text,
                )

                if (actionButton != null) {

                    val isEnabled = actionButton.isEnabled

                    Text(
                        text = actionButton.text,
                        modifier = Modifier
                            .padding(end = H_PADDING)
                            .clip(roundedShape)
                            .background(if (isEnabled) c.blue else Color.DarkGray)
                            .clickable(isEnabled) {
                                actionButton.onClick()
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
}

///

@Composable
private fun TopButtonView(
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(roundedShape)
            .clickable {
                onClick()
            }
            .padding(horizontal = H_PADDING_HALF),
        color = color,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
    )
}
