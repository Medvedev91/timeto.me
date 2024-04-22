package me.timeto.app.ui

import android.widget.CalendarView
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import me.timeto.app.*
import me.timeto.shared.UnixTime
import me.timeto.shared.db.EventDb
import me.timeto.shared.vm.EventFormSheetVM
import java.text.SimpleDateFormat
import java.util.*

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

    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    var isFirstImeHideSkipped by remember { mutableStateOf(false) }
    LaunchedEffect(state.selectedTime) {
        if (!isFirstImeHideSkipped) {
            isFirstImeHideSkipped = true
            return@LaunchedEffect
        }
        keyboardController?.hide()
    }

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress)
            keyboardController?.hide()
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.sheetBg)
    ) {

        Column(
            modifier = Modifier
                .verticalScroll(
                    state = scrollState
                )
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {

            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Icon(
                    Icons.Rounded.Close,
                    "Close",
                    tint = c.text.copy(alpha = 0.4f),
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(36.dp)
                        .clip(roundedShape)
                        .background(c.transparent)
                        .clickable {
                            layer.close()
                        }
                        .padding(6.dp)
                )

                MyListView__ItemView(
                    modifier = Modifier.weight(1f),
                    isFirst = true,
                    isLast = true,
                    outerPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    MyListView__ItemView__TextInputView(
                        placeholder = "Event Title",
                        text = state.inputTextValue,
                        onTextChanged = { vm.setInputTextValue(it) },
                        isAutofocus = state.isAutoFocus,
                        keyboardButton = ImeAction.Done,
                        keyboardEvent = { keyboardController?.hide() },
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

            AndroidView(
                { CalendarView(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-2).dp),
                update = { calendarView ->
                    calendarView.date = state.selectedTime * 1000L
                    calendarView.minDate = state.minTime * 1000L
                    calendarView.setOnDateChangeListener { _, cYear, cMonthIndex, cDay ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val newDate = formatter.parse("$cYear-${cMonthIndex + 1}-${cDay}")!!
                        val newDayStartTime = UnixTime((newDate.time / 1000L).toInt()).localDayStartTime()
                        vm.setTimeByComponents(dayStartTime = newDayStartTime)
                    }
                }
            )

            DayTimePickerView(
                hour = state.hour,
                minute = state.minute,
                onHourChanged = { hour -> vm.setTimeByComponents(hour = hour) },
                onMinuteChanged = { minute -> vm.setTimeByComponents(minute = minute) },
            )
        }
    }
}
