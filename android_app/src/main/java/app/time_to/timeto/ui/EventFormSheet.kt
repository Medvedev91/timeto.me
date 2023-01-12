package app.time_to.timeto.ui

import android.widget.CalendarView
import android.widget.NumberPicker
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import app.time_to.timeto.*
import app.time_to.timeto.R
import timeto.shared.UnixTime
import timeto.shared.db.EventModel
import timeto.shared.vm.EventFormSheetVM
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EventFormSheet(
    isPresented: MutableState<Boolean>,
    editedEvent: EventModel?,
    defText: String? = null,
    defTime: Int? = null,
    onSave: () -> Unit,
) {

    TimetoSheet(
        state = isPresented,
        topPadding = 2.dp, // To fit calendar over the keyboard
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
                .background(c.bgFormSheet)
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
                        painterResource(id = R.drawable.ic_round_close_24),
                        "Close",
                        tint = c.text.copy(alpha = 0.4f),
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(c.transparent)
                            .clickable {
                                isPresented.value = false
                            }
                            .padding(6.dp)
                    )

                    MyListView__SectionView(
                        modifier = Modifier.weight(1f),
                        paddingStart = 8.dp,
                        paddingEnd = 10.dp,
                    ) {
                        MyListView__SectionView__TextInputView(
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
                            .clip(RoundedCornerShape(99.dp))
                            .clickable(enabled = state.isHeaderDoneEnabled) {
                                vm.save {
                                    isPresented.value = false
                                    onSave()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        color = animateColorAsState(targetValue = if (state.isHeaderDoneEnabled) c.blue else c.textSecondary.copy(alpha = 0.4f)).value,
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    Box(
                        modifier = Modifier.width(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxWidth(),
                            factory = { context ->
                                val hourIndexes = 0..23
                                NumberPicker(context).apply {
                                    setOnValueChangedListener { _, _, new ->
                                        vm.setTimeByComponents(hour = new)
                                    }
                                    displayedValues = hourIndexes.map { "$it".padStart(2, '0') }.toTypedArray()
                                    if (isSDKQPlus())
                                        textSize = dpToPx(18f).toFloat()
                                    wrapSelectorWheel = false
                                    minValue = 0
                                    maxValue = hourIndexes.last
                                    value = state.hour // Set last
                                }
                            }
                        )
                    }

                    Box(
                        modifier = Modifier.width(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxWidth(),
                            factory = { context ->
                                val minuteIndexes = 0..59
                                NumberPicker(context).apply {
                                    setOnValueChangedListener { _, _, new ->
                                        vm.setTimeByComponents(minute = new)
                                    }
                                    displayedValues = minuteIndexes.map { "$it".padStart(2, '0') }.toTypedArray()
                                    if (isSDKQPlus())
                                        textSize = dpToPx(18f).toFloat()
                                    wrapSelectorWheel = false
                                    minValue = 0
                                    maxValue = minuteIndexes.last
                                    value = state.minute // Set last
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
