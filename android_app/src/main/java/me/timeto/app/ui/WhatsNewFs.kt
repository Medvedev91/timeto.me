package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.vm.ReadmeVm
import me.timeto.shared.vm.WhatsNewVm

@Composable
fun WhatsNewFs(
    layer: WrapperView.Layer,
) {

    val (_, state) = rememberVm {
        WhatsNewVm()
    }

    VStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.bg),
    ) {

        val scrollState = rememberScrollState()

        Fs__HeaderTitle(
            title = state.headerTitle,
            scrollState = scrollState,
            onClose = {
                layer.close()
            },
        )

        VStack(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .weight(1f),
        ) {

            state.historyItemsUi.forEach { historyItemUi ->

                VStack(
                    modifier = Modifier
                        .padding(top = 12.dp),
                ) {

                    HStack(
                        modifier = Modifier
                            .padding(horizontal = H_PADDING),
                    ) {

                        Text(
                            text = historyItemUi.dateText,
                            modifier = Modifier
                                .weight(1f),
                            color = c.textSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Light,
                        )

                        Text(
                            text = historyItemUi.timeAgoText,
                            color = c.textSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Light,
                        )
                    }

                    Text(
                        text = historyItemUi.title,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .padding(horizontal = H_PADDING),
                        color = c.text,
                        fontWeight = FontWeight.Bold,
                    )

                    val text = historyItemUi.text
                    if (text != null) {
                        Text(
                            text = text,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .padding(horizontal = H_PADDING),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Light,
                            color = c.textSecondary,
                        )
                    }

                    val buttonUi = historyItemUi.buttonUi
                    if (buttonUi != null) {
                        Text(
                            text = buttonUi.text,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .padding(horizontal = H_PADDING_HALF)
                                .clip(squircleShape)
                                .clickable {
                                    when (buttonUi) {
                                        WhatsNewVm.HistoryItemUi.ButtonUi.pomodoro -> {
                                            ReadmeSheet__show(ReadmeVm.DefaultItem.pomodoro)
                                        }
                                    }
                                }
                                .padding(horizontal = H_PADDING_HALF - halfDpFloor),
                            color = c.blue,
                        )
                    }

                    if (state.historyItemsUi.last() != historyItemUi)
                        DividerBg(
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .padding(horizontal = H_PADDING),
                        )
                }
            }

            ZStack(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = H_PADDING),
            ) {}
        }
    }
}
