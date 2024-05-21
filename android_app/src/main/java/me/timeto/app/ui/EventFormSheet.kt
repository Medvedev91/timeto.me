package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
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
    Fs.show { layer ->
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

    VStack(
        modifier = Modifier
            .fillMaxSize()
            .background(c.sheetBg),
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

            DateTimeButton(
                text = state.selectedDateText,
                paddingStart = H_PADDING,
                onClick = {
                    Dialog.showDatePicker(
                        unixTime = state.selectedUnixTime,
                        minTime = UnixTime(state.minTime),
                        maxTime = UnixTime(UnixTime.MAX_TIME),
                        onSelect = {
                            vm.setUnixDay(it.localDay)
                        },
                    )
                },
            )

            DaytimePickerSliderView(
                daytimeModel = state.daytimeModel,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 10.dp),
                onChange = { daytimeModel ->
                    vm.setDaytime(daytimeModel)
                },
            )
        }

        EventTemplatesView(
            modifier = Modifier
                .padding(bottom = 8.dp),
            onPick = { templateUi ->
                vm.setTemplate(templateUi)
            },
        )

        HStack(
            modifier = Modifier
                .background(c.sheetFg)
                .padding(vertical = 4.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            ZStack(
                modifier = Modifier
                    .weight(1f),
            ) {

                MyListView__ItemView__TextInputView(
                    placeholder = state.inputPlaceholder,
                    text = state.inputTextValue,
                    onTextChanged = { vm.setInputTextValue(it) },
                    isAutofocus = true,
                    keyboardButton = ImeAction.Done,
                    keyboardEvent = {},
                )
            }

            Text(
                text = state.saveText,
                modifier = Modifier
                    .padding(start = 10.dp, end = H_PADDING)
                    .clip(roundedShape)
                    .background(c.blue)
                    .clickable {
                        vm.save {
                            layer.close()
                            onSave()
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                color = c.white,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun DateTimeButton(
    text: String,
    paddingStart: Dp,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        modifier = Modifier
            .padding(start = paddingStart)
            .clip(squircleShape)
            .background(c.sheetFg)
            .clickable {
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .padding(top = onePx),
        color = c.text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
    )
}
