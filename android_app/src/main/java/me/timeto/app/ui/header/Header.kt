package me.timeto.app.ui.header

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.HStack
import me.timeto.app.H_PADDING
import me.timeto.app.ui.header.views.HeaderCloseButtonView
import me.timeto.app.ui.header.views.HeaderTitleView
import me.timeto.app.ui.header.views.HeaderView

val Header__titleFontSize = 26.sp // Golden ratio to list's text
val Header__titleFontWeight = FontWeight.ExtraBold

val Header__buttonFontSize = 15.sp

@Composable
fun Header(
    title: String,
    scrollState: ScrollableState?,
    onClose: () -> Unit,
) {

    HeaderView(
        scrollState = scrollState,
    ) {

        HStack(
            modifier = Modifier
                .padding(top = 20.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            HeaderTitleView(
                title = title,
            )

            HeaderCloseButtonView(
                modifier = Modifier
                    .padding(end = H_PADDING),
            ) {
                onClose()
            }
        }
    }
}