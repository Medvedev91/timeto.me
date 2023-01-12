package app.time_to.timeto.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

private val LocalTimetoSheet = compositionLocalOf<MutableList<TimetoSheetItem>> { mutableListOf() }

private class TimetoSheetItem(
    val isShowed: MutableState<Boolean>,
    val shape: Shape,
    val topPadding: Dp,
    val sheetContent: @Composable () -> Unit
)

/**
 * https://developer.android.com/jetpack/compose/gestures#auto-nested-scrolling
 */
@Composable
fun TimetoSheetLayout(
    content: @Composable () -> Unit,
) {
    val localBottomSheetState = remember { mutableStateListOf<TimetoSheetItem>() }

    CompositionLocalProvider(
        LocalTimetoSheet provides localBottomSheetState,
    ) {

        Box {

            content()

            localBottomSheetState.forEach { item ->

                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AnimatedVisibility(
                        item.isShowed.value,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x55000000))
                                .clickable { item.isShowed.value = false }
                        )
                    }

                    AnimatedVisibility(
                        item.isShowed.value,
                        enter = slideInVertically(
                            animationSpec = spring(
                                stiffness = Spring.StiffnessMedium,
                                visibilityThreshold = IntOffset.VisibilityThreshold
                            ),
                            initialOffsetY = { it }
                        ),
                        exit = slideOutVertically(
                            animationSpec = spring(
                                stiffness = Spring.StiffnessMedium,
                                visibilityThreshold = IntOffset.VisibilityThreshold
                            ),
                            targetOffsetY = { it }
                        ),
                    ) {
                        Box(
                            /**
                             * Ordering is important. Otherwise podding
                             * by height wouldn't work on close click.
                             */
                            modifier = Modifier
                                // Restriction max height
                                .statusBarsPadding()
                                .padding(top = item.topPadding)
                                ////
                                .clip(item.shape)
                                .pointerInput(Unit) { }
                        ) {
                            item.sheetContent()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimetoSheet(
    state: MutableState<Boolean>,
    topPadding: Dp = 20.dp,
    sheetContent: @Composable () -> Unit
) {
    BackHandler(state.value) {
        state.value = false
    }

    val local = LocalTimetoSheet.current
    val item = TimetoSheetItem(
        isShowed = state,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        topPadding = topPadding,
        sheetContent = sheetContent
    )
    DisposableEffect(Unit) {
        local.add(item)
        onDispose {
            local.remove(item)
        }
    }
}
