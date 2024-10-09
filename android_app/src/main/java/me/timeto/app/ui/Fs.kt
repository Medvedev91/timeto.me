package me.timeto.app.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import me.timeto.app.*
import me.timeto.app.R

private val enterAnimation = fadeIn(spring(stiffness = Spring.StiffnessMedium))
private val exitAnimation = fadeOut(spring(stiffness = Spring.StiffnessMedium))

val Fs__TITLE_FONT_SIZE = 26.sp // Golden ratio to lists text
val Fs__TITLE_FONT_WEIGHT = FontWeight.ExtraBold
val Fs__BUTTON_FONT_SIZE = 15.sp

object Fs {

    fun show(
        content: @Composable (WrapperView.Layer) -> Unit,
    ) {

        WrapperView.Layer(
            enterAnimation = enterAnimation,
            exitAnimation = exitAnimation,
            alignment = Alignment.BottomCenter,
            onClose = {},
            content = { layer ->
                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {}
                ) {
                    content(layer)
                }
            }
        ).show()
    }
}

@Composable
fun Fs__CloseButton(
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
            painterResource(R.drawable.sf_xmark_small_medium),
            contentDescription = "Close",
            tint = c.tertiaryText,
            modifier = Modifier
                .size(11.dp),
        )
    }
}

@Composable
fun Fs__Header(
    scrollState: ScrollableState?,
    content: @Composable () -> Unit,
) {

    ZStack(
        modifier = Modifier
            .padding(top = (LocalContext.current as MainActivity).statusBarHeightDp),
    ) {

        content()

        DividerBgScroll(
            scrollState = scrollState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = H_PADDING),
        )
    }
}

@Composable
fun Fs__HeaderTitle(
    title: String,
    scrollState: ScrollableState?,
    onClose: () -> Unit,
) {

    Fs__Header(
        scrollState = scrollState,
    ) {

        HStack(
            modifier = Modifier
                .padding(top = 20.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            HeaderTitle(
                title = title,
            )

            Fs__CloseButton(
                modifier = Modifier
                    .padding(end = H_PADDING),
            ) {
                onClose()
            }
        }
    }
}

@Composable
fun Fs__HeaderAction(
    title: String,
    actionText: String,
    scrollState: ScrollableState?,
    onCancel: () -> Unit,
    onDone: () -> Unit,
) {

    Fs__Header(
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

                HeaderTitle(
                    title = title,
                )

                Text(
                    text = actionText,
                    modifier = Modifier
                        .padding(end = H_PADDING)
                        .clip(roundedShape)
                        .background(c.blue)
                        .clickable {
                            onDone()
                        }
                        .padding(
                            horizontal = 10.dp,
                            vertical = 3.dp,
                        ),
                    color = c.text,
                    fontSize = Fs__BUTTON_FONT_SIZE,
                    fontWeight = Fs__TITLE_FONT_WEIGHT,
                )
            }
        }
    }
}

@Composable
fun Fs__HeaderClose(
    onClose: () -> Unit,
) {

    HStack(
        modifier = Modifier
            .padding(top = (LocalContext.current as MainActivity).statusBarHeightDp + H_PADDING)
            .zIndex(1f),
    ) {

        SpacerW1()

        Fs__CloseButton(
            modifier = Modifier
                .padding(end = H_PADDING),
        ) {
            onClose()
        }
    }
}

@Composable
private fun RowScope.HeaderTitle(
    title: String,
) {
    Text(
        text = title,
        modifier = Modifier
            .padding(start = H_PADDING)
            .weight(1f),
        fontSize = Fs__TITLE_FONT_SIZE,
        fontWeight = Fs__TITLE_FONT_WEIGHT,
        color = c.text,
    )
}

//
// Bottom

@Composable
fun Fs__BottomBar(
    content: @Composable () -> Unit,
) {

    VStack(
        modifier = Modifier
            .navigationBarsPadding(),
    ) {

        DividerBg()

        content()
    }
}
