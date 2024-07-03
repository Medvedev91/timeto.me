package me.timeto.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import me.timeto.app.ZStack
import me.timeto.app.c
import me.timeto.app.onePx

@Composable
fun DividerBgScroll(
    scrollState: ScrollableState?,
    modifier: Modifier = Modifier,
) {

    val alphaValue = remember {

        derivedStateOf {

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

                else -> throw Exception("todo FS.kt")
            }
        }
    }

    val alphaAnimate = animateFloatAsState(alphaValue.value)

    ZStack(
        modifier = modifier
            .height(onePx)
            .fillMaxWidth()
            .drawBehind {
                drawRect(color = c.dividerBg.copy(alpha = alphaAnimate.value))
            },
    )
}

private const val animRatio = 50f
