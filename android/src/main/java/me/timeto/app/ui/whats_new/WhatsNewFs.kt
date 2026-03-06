package me.timeto.app.ui.whats_new

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.R
import me.timeto.app.openGooglePlayAppPage
import me.timeto.app.ui.Divider
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.H_PADDING_HALF
import me.timeto.app.ui.Screen
import me.timeto.app.ui.VStack
import me.timeto.app.ui.c
import me.timeto.app.ui.donations.DonationsFs
import me.timeto.app.ui.halfDpFloor
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.readme.ReadmeFs
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.squircleShape
import me.timeto.shared.SystemInfo
import me.timeto.shared.vm.readme.ReadmeVm
import me.timeto.shared.vm.whats_new.WhatsNewVm

@Composable
fun WhatsNewFs() {

    val context = LocalContext.current
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

                val historyItemsUi = state.historyItemsUi
                historyItemsUi.forEach { historyItemUi ->

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
                                color = c.secondaryText,
                                fontWeight = FontWeight.Light,
                            )

                            Text(
                                text = historyItemUi.timeAgoText,
                                color = c.secondaryText,
                                fontWeight = FontWeight.Light,
                            )
                        }

                        Text(
                            text = historyItemUi.title,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .padding(horizontal = H_PADDING),
                            color = c.text,
                            fontWeight = FontWeight.SemiBold,
                        )

                        val text = historyItemUi.text
                        if (text != null) {
                            Text(
                                text = text,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .padding(horizontal = H_PADDING),
                                fontWeight = FontWeight.Light,
                                color = c.secondaryText,
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

                        if (historyItemsUi[0] == historyItemUi) {
                            HStack(
                                modifier = Modifier
                                    .padding(top = 4.dp, start = H_PADDING_HALF),
                            ) {
                                ButtonView(
                                    text = "Rate",
                                    color = c.blue,
                                    iconRes = R.drawable.sf_star_fill_medium_bold,
                                    iconOffset = (-1).dp,
                                    iconSize = 21.dp,
                                    spacer = 5.dp,
                                    onClick = {
                                        openGooglePlayAppPage(context = context)
                                    },
                                )
                                if (SystemInfo.instance.isFdroid) {
                                    ButtonView(
                                        text = "Donate",
                                        color = c.red,
                                        iconRes = R.drawable.sf_heart_fill_medium_regular,
                                        iconOffset = 0.dp,
                                        iconSize = 20.dp,
                                        spacer = 6.dp,
                                        onClick = {
                                            navigationFs.push {
                                                DonationsFs()
                                            }
                                        },
                                    )
                                }
                            }
                        }

                        if (historyItemsUi.last() != historyItemUi) {
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

@Composable
private fun ButtonView(
    text: String,
    color: Color,
    @DrawableRes iconRes: Int,
    iconOffset: Dp,
    iconSize: Dp,
    spacer: Dp,
    onClick: () -> Unit,
) {
    HStack(
        modifier = Modifier
            .clip(squircleShape)
            .clickable {
                onClick()
            }
            .padding(horizontal = H_PADDING_HALF, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            modifier = Modifier
                .offset(y = iconOffset)
                .size(iconSize),
            tint = color,
        )
        Text(
            text = text,
            modifier = Modifier
                .padding(start = spacer),
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
