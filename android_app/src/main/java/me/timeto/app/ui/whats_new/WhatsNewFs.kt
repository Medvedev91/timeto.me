package me.timeto.app.ui.whats_new

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.ui.Divider
import me.timeto.app.ui.Screen
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.readme.ReadmeFs
import me.timeto.shared.vm.ReadmeVm
import me.timeto.shared.vm.WhatsNewVm

@Composable
fun WhatsNewFs() {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (_, state) = rememberVm {
        WhatsNewVm()
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = null,
            cancelButton = HeaderCancelButton(
                text = "Close",
                onClick = {
                    navigationLayer.close()
                },
            ),
        )

        LazyColumn(
            state = scrollState,
            contentPadding = PaddingValues(bottom = (H_PADDING * 2)),
        ) {

            item {

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
                                                navigationFs.push {
                                                    ReadmeFs(
                                                        defaultItem = ReadmeVm.DefaultItem.pomodoro,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    .padding(horizontal = H_PADDING_HALF - halfDpFloor),
                                color = c.blue,
                            )
                        }

                        if (state.historyItemsUi.last() != historyItemUi) {
                            Divider(
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .padding(start = H_PADDING),
                            )
                        }
                    }
                }
            }
        }
    }
}
