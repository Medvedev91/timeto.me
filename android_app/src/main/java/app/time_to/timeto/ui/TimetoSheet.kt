package app.time_to.timeto.ui

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
import androidx.compose.ui.unit.dp

@Composable
fun TimetoSheet(
    state: MutableState<Boolean>,
    topPadding: Dp = 20.dp,
    sheetContent: @Composable () -> Unit
) {
    UIWrapper.LayerView(
        UIWrapper.LayerData(
            isPresented = state,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
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
