package me.timeto.app.ui.footer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import me.timeto.app.HStack
import me.timeto.app.ZStack
import me.timeto.app.onePx
import me.timeto.app.ui.main.MainTabsView__HEIGHT
import me.timeto.app.ui.main.MainTabsView__backgroundColor
import me.timeto.app.ui.main.MainTabsView__dividerColor

@Composable
fun Footer(
    scrollState: LazyListState?,
    content: @Composable RowScope.() -> Unit,
) {

    val alphaValue = remember {
        derivedStateOf {
            when {
                scrollState == null -> 0f
                scrollState.canScrollForward -> 1f
                else -> 0f
            }
        }
    }

    val alphaAnimate = animateFloatAsState(alphaValue.value)

    ZStack(
        modifier = Modifier
            .drawBehind {
                drawRect(color = MainTabsView__backgroundColor.copy(alpha = alphaAnimate.value))
            }
            .navigationBarsPadding()
            .height(MainTabsView__HEIGHT),
    ) {

        ZStack(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .height(onePx)
                .fillMaxWidth()
                .drawBehind {
                    drawRect(color = MainTabsView__dividerColor.copy(alpha = alphaAnimate.value))
                },
        )

        HStack(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}
