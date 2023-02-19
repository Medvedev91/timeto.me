package app.time_to.timeto.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun TimetoSheet(
    isPresented: MutableState<Boolean>,
    topPadding: Dp = 20.dp,
    sheetContent: @Composable () -> Unit
) {
    UIWrapper.LayerView(
        UIWrapper.LayerData(
            isPresented = isPresented,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
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
            content = {
                Box(
                    /**
                     * Ordering is important. Otherwise, podding
                     * by height wouldn't work on close click.
                     */
                    modifier = Modifier
                        // Restriction max height
                        .statusBarsPadding()
                        .padding(top = topPadding)
                        ////
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .pointerInput(Unit) { }
                ) {
                    sheetContent()
                }
            }
        )
    )
}
