package me.timeto.app.ui.privacy

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.HStack
import me.timeto.app.H_PADDING
import me.timeto.app.H_PADDING_HALF
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.app.showOpenSource
import me.timeto.app.squircleShape
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.form.FormSwitch
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.ui.privacy.PrivacyVm

@Composable
fun PrivacyFs(
    isFdroid: Boolean,
) {

    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        PrivacyVm()
    }

    BackHandler(enabled = isFdroid) {}

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton =
                if (isFdroid && !state.isSendingReportsEnabled) null
                else HeaderActionButton(
                    text = "Done",
                    isEnabled = true,
                    onClick = {
                        navigationLayer.close()
                    },
                )
            ,
            cancelButton =
                if (isFdroid) null
                else HeaderCancelButton(
                    text = "Close",
                    onClick = {
                        navigationLayer.close()
                    },
                ),
        )

        LazyColumn(
            state = scrollState,
        ) {

            state.textsUi.forEach { textUi ->
                item {
                    Text(
                        text = textUi.text,
                        modifier = Modifier
                            .padding(horizontal = H_PADDING)
                            .padding(top = 16.dp),
                        color = c.text,
                        lineHeight = 22.sp,
                        fontWeight =
                            if (textUi.isBold) FontWeight.Bold
                            else FontWeight.Normal,
                    )
                }
            }

            item {
                FormSwitch(
                    title = state.sendReportsTitle,
                    isEnabled = state.isSendingReportsEnabled,
                    isFirst = true,
                    isLast = true,
                    modifier = Modifier
                        .padding(top = 20.dp),
                    onChange = { newValue ->
                        vm.setIsSendingReports(isEnabled = newValue)
                    },
                )
            }

            item {

                HStack {

                    BottomButton(
                        text = "Open Source",
                        color = c.blue,
                        onClick = {
                            showOpenSource()
                        },
                    )

                    SpacerW1()

                    if (isFdroid && !state.isSendingReportsEnabled) {
                        BottomButton(
                            text = "Don't Send",
                            color = c.textSecondary,
                            onClick = {
                                vm.setIsSendingReports(isEnabled = false)
                                navigationLayer.close()
                            },
                        )
                    }
                }
            }
        }
    }
}

///

@Composable
private fun BottomButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        modifier = Modifier
            .padding(top = 12.dp)
            .padding(horizontal = H_PADDING_HALF)
            .clip(squircleShape)
            .clickable {
                onClick()
            }
            .padding(horizontal = H_PADDING_HALF, vertical = 4.dp),
        color = color,
    )
}
