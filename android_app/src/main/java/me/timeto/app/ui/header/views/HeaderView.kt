package me.timeto.app.ui.header.views

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import me.timeto.app.H_PADDING
import me.timeto.app.MainActivity
import me.timeto.app.ZStack
import me.timeto.app.ui.DividerBgScroll

@Composable
fun HeaderView(
    scrollState: ScrollableState?,
    content: @Composable () -> Unit,
) {

    val mainActivity = LocalContext.current as MainActivity

    ZStack(
        modifier = Modifier
            .padding(top = mainActivity.statusBarHeightDp),
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
