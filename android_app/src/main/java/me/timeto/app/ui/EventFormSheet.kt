package me.timeto.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.UnixTime
import me.timeto.shared.db.EventDb
import me.timeto.shared.vm.EventFormSheetVM

fun EventFormSheet__show(
    editedEvent: EventDb?,
    defText: String? = null,
    defTime: Int? = null,
    onSave: () -> Unit,
) {
    FullScreen.show { layer ->
        EventFormSheet(
            layer = layer,
            editedEvent = editedEvent,
            defText = defText,
            defTime = defTime,
            onSave = onSave,
        )
    }
}

@Composable
private fun EventFormSheet(
    layer: WrapperView.Layer,
    editedEvent: EventDb?,
    defText: String? = null,
    defTime: Int? = null,
    onSave: () -> Unit,
) {

    val (vm, state) = rememberVM(editedEvent, defText, defTime) {
        EventFormSheetVM(
            event = editedEvent,
            defText = defText,
            defTime = defTime,
        )
    }

    val scrollState = rememberScrollState()

    ZStack(
        modifier = Modifier
            .fillMaxSize()
            .background(c.sheetBg)
    ) {

        VStack(
            modifier = Modifier
                .fillMaxSize()
        ) {

            VStack(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(
                        state = scrollState,
                        reverseScrolling = true,
                    )
            ) {

                SpacerW1()

                Text(
                    text = state.selectedTimeText,
                    modifier = Modifier
                        .padding(start = H_PADDING)
                        .clip(squircleShape)
                        .background(c.sheetFg)
                        .clickable {
                            Dialog.showDatePicker(
                                unixTime = state.selectedUnixTime,
                                minTime = UnixTime(state.minTime),
                                maxTime = UnixTime(UnixTime.MAX_TIME),
                                onSelect = {
                                    vm.setUnixDay(it.localDay)
                                },
                            )
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .padding(top = onePx),
                    color = c.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )

                DaytimePickerSliderView(
                    daytimePickerUi = state.daytimePickerUi,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp),
                    onChange = { newDaytimePickerUi ->
                        vm.setDaytimePickerUi(newDaytimePickerUi)
                    },
                )
            }

            HStack(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                MyListView__ItemView(
                    modifier = Modifier.weight(1f),
                    isFirst = true,
                    isLast = true,
                    outerPadding = PaddingValues(start = H_PADDING, end = 8.dp)
                ) {
                    MyListView__ItemView__TextInputView(
                        placeholder = "Event",
                        text = state.inputTextValue,
                        onTextChanged = { vm.setInputTextValue(it) },
                        isAutofocus = true,
                        keyboardButton = ImeAction.Done,
                        keyboardEvent = {},
                    )
                }

                Text(
                    state.headerDoneText,
                    modifier = Modifier
                        .padding(end = 10.dp, bottom = 1.dp)
                        .clip(roundedShape)
                        .clickable(enabled = state.isHeaderDoneEnabled) {
                            vm.save {
                                layer.close()
                                onSave()
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = animateColorAsState(
                        targetValue =
                        if (state.isHeaderDoneEnabled) c.blue
                        else c.textSecondary.copy(alpha = 0.4f)
                    ).value,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W600
                )
            }
        }

        Icon(
            Icons.Rounded.Close,
            "Close",
            tint = c.textSecondary.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = statusBarHeight + 2.dp, end = H_PADDING)
                .size(36.dp)
                .clip(roundedShape)
                .background(c.sheetFg)
                .clickable {
                    layer.close()
                }
                .padding(6.dp),
        )
    }
}
