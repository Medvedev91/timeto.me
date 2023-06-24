package me.timeto.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*

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

    // todo remove
    @Composable
    fun HeaderViewOld(
        onCancel: () -> Unit,
        title: String,
        doneText: String?,
        isDoneEnabled: Boolean,
        scrollState: ScrollableState?,
        cancelText: String = "Cancel",
        bgColor: Color = c.formHeaderBackground,
        dividerColor: Color = c.formHeaderDivider,
        maxLines: Int = Int.MAX_VALUE,
        onDone: () -> Unit,
    ) {
        val alphaValue = remember {
            derivedStateOf {
                val animRatio = 50f
                when (scrollState) {
                    null -> 0f
                    is LazyListState -> {
                        val offset = scrollState.firstVisibleItemScrollOffset
                        when {
                            scrollState.firstVisibleItemIndex > 0 -> 1f
                            offset == 0 -> 0f
                            offset > animRatio -> 1f
                            else -> offset / animRatio
                        }
                    }
                    is ScrollState -> {
                        val offset = scrollState.value
                        when {
                            offset == 0 -> 0f
                            offset > animRatio -> 1f
                            else -> offset / animRatio
                        }
                    }
                    else -> throw Exception("todo Sheet.kt")
                }
            }
        }
        val alphaAnimate = animateFloatAsState(alphaValue.value)

        Box(
            modifier = Modifier
                .drawBehind {
                    drawRect(color = bgColor.copy(alpha = alphaAnimate.value))
                },
            contentAlignment = Alignment.BottomCenter // For divider
        ) {

            Box(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 16.dp),
            ) {

                Text(
                    cancelText,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .clickable { onCancel() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = c.blue,
                    fontSize = 16.sp,
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

                if (doneText != null)
                    Text(
                        doneText,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 18.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .clickable(enabled = isDoneEnabled) {
                                onDone()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        color = animateColorAsState(
                            targetValue = if (isDoneEnabled) c.blue
                            else c.textSecondary.copy(alpha = 0.4f)
                        ).value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
            }

            ZStack(
                modifier = Modifier
                    .height(onePx)
                    .fillMaxWidth()
                    .drawBehind {
                        drawRect(color = dividerColor.copy(alpha = alphaAnimate.value))
                    },
            )
        }
    }
}

@Composable
fun Sheet__HeaderView(
    title: String,
    scrollState: ScrollableState?,
) {
    val alphaValue = remember {
        derivedStateOf {
            val animRatio = 50f
            when (scrollState) {
                null -> 0f
                is LazyListState -> {
                    val offset = scrollState.firstVisibleItemScrollOffset
                    when {
                        scrollState.firstVisibleItemIndex > 0 -> 1f
                        offset == 0 -> 0f
                        offset > animRatio -> 1f
                        else -> offset / animRatio
                    }
                }
                is ScrollState -> {
                    val offset = scrollState.value
                    when {
                        offset == 0 -> 0f
                        offset > animRatio -> 1f
                        else -> offset / animRatio
                    }
                }
                else -> throw Exception("todo Sheet__HeaderView")
            }
        }
    }
    val alphaAnimate = animateFloatAsState(alphaValue.value)
    val bgColor = c.bg
    val dividerBgColor = c.dividerBg

    ZStack(
        modifier = Modifier
            .drawBehind {
                drawRect(color = bgColor.copy(alpha = alphaAnimate.value))
            },
    ) {

        Text(
            text = title,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            fontSize = 23.sp,
            fontWeight = FontWeight.SemiBold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = c.text,
            textAlign = TextAlign.Center,
        )

        ZStack(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(onePx)
                .fillMaxWidth()
                .drawBehind {
                    drawRect(color = dividerBgColor.copy(alpha = alphaAnimate.value))
                },
        )
    }
}

///
/// Sheet__BottomView

@Composable
fun Sheet__BottomView(
    content: @Composable () -> Unit,
) {
    VStack(
        modifier = Modifier
            .background(c.bg)
            .navigationBarsPadding(),
    ) {
        DividerBg()
        content()
    }
}

@Composable
fun Sheet__BottomViewDefault(
    primaryText: String,
    primaryAction: () -> Unit,
    secondaryText: String,
    secondaryAction: () -> Unit,
    topContent: (@Composable () -> Unit)? = null,
    startContent: (@Composable () -> Unit)? = null,
) {
    Sheet__BottomView {
        topContent?.invoke()
        HStack(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            startContent?.invoke()
            SpacerW1()
            HStack(
                modifier = Modifier
                    .padding(
                        top = 10.dp,
                        end = MyListView.PADDING_OUTER_HORIZONTAL,
                        bottom = 10.dp,
                    )
            ) {
                Sheet__BottomView__SecondaryButton(secondaryText) {
                    secondaryAction()
                }
                Sheet__BottomView__PrimaryButton(primaryText) {
                    primaryAction()
                }
            }
        }
    }
}

///

@Composable
fun Sheet__BottomView__Button(
    text: String,
    modifier: Modifier,
    backgroundColor: Color,
    fontColor: Color,
    fontWeight: FontWeight,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        modifier = modifier
            .clip(roundedShape)
            .background(backgroundColor)
            .clickable {
                onClick()
            }
            .padding(horizontal = 14.dp)
            .padding(top = 6.dp, bottom = 7.dp),
        color = fontColor,
        fontSize = 15.sp,
        fontWeight = fontWeight,
    )
}

@Composable
fun Sheet__BottomView__PrimaryButton(
    text: String,
    onClick: () -> Unit,
) {
    Sheet__BottomView__Button(
        text = text,
        modifier = Modifier,
        backgroundColor = c.blue,
        fontColor = c.white,
        fontWeight = FontWeight.SemiBold,
    ) {
        onClick()
    }
}

@Composable
fun Sheet__BottomView__SecondaryButton(
    text: String,
    withPaddingRight: Boolean = true,
    onClick: () -> Unit,
) {
    Sheet__BottomView__Button(
        text = text,
        modifier = Modifier
            .padding(end = if (withPaddingRight) 6.dp else 0.dp),
        backgroundColor = c.transparent,
        fontColor = c.textSecondary,
        fontWeight = FontWeight.Light,
    ) {
        onClick()
    }
}
