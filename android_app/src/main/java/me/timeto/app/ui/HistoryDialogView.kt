package me.timeto.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.*
import me.timeto.shared.vm.HistoryVm

@Composable
fun HistoryDialogView(
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var isEditMode by remember { mutableStateOf(false) }

    val (vm, state) = rememberVm { HistoryVm() }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.background(c.sheetBg),
    ) {

        val scrollState = rememberLazyListState()
        var listHeight by remember { mutableStateOf(0) }
        val listContentPadding = PaddingValues(top = 20.dp, bottom = 70.dp)

        /**
         * WARNING
         * To adding items to the list you have to refactor
         * the calculation for scrolling to the date.
         */
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .onGloballyPositioned {
                    listHeight = it.size.height
                },
            state = scrollState,
            reverseLayout = true,
            contentPadding = listContentPadding
        ) {

            state.sections.reversed().forEach { section ->

                itemsIndexed(
                    section.intervals.reversed(),
                    // Same interval can be in different days. Using day to set unique keys.
                    key = { _, interval -> "${section.day}__${interval.id}" }
                ) { _, interval ->

                    val intervalUI = remember(interval) { HistoryVm.IntervalUI.build(interval, section) }
                    val timeHeight = (intervalUI.secondsForBar / 60).dp // 60 is the coefficient, you can change it

                    Row(
                        verticalAlignment = Alignment.Top
                    ) {

                        Row(
                            modifier = Modifier
                                .width(100.dp)
                        ) {

                            if (!intervalUI.isStartsPrevDay) {

                                AnimatedVisibility(isEditMode) {
                                    Row(modifier = Modifier.padding(top = 1.dp)) {
                                        Icon(
                                            Icons.Rounded.RemoveCircle,
                                            "Delete",
                                            tint = c.red,
                                            modifier = Modifier
                                                .padding(top = 1.dp, start = 8.dp)
                                                .size(22.dp)
                                                .clip(roundedShape)
                                                .clickable {
                                                    intervalUI.delete()
                                                }
                                        )
                                        Icon(
                                            painterResource(id = R.drawable.sf_pencil_medium_medium),
                                            "Edit",
                                            tint = c.blue,
                                            modifier = Modifier
                                                .padding(top = 1.dp, start = 5.dp)
                                                .size(24.dp)
                                                .clip(roundedShape)
                                                .clickable {
                                                    Dialog.showDateTimePicker(
                                                        defaultTime = interval.unixTime(),
                                                        minPickableDay = state.minPickerDay,
                                                        minSavableDay = state.minPickerDay,
                                                        maxDay = UnixTime().localDay,
                                                        withTimeBtnText = "Save",
                                                        onSelect = { selectedTime ->
                                                            intervalUI.upTime(selectedTime)
                                                        }
                                                    )

                                                }
                                                .padding(5.dp)
                                        )
                                    }
                                }

                                AnimatedVisibility(!isEditMode) {
                                    Text(
                                        intervalUI.periodString,
                                        modifier = Modifier.padding(top = 5.2.dp, start = 16.dp),
                                        textAlign = TextAlign.End,
                                        fontWeight = FontWeight.W300,
                                        fontSize = 10.5.sp,
                                        color = c.textSecondary
                                    )
                                }

                                Text(
                                    intervalUI.timeString,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .weight(1f),
                                    textAlign = TextAlign.End,
                                    fontWeight = FontWeight.W600,
                                    fontSize = 13.sp,
                                    color = c.textSecondary,
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                        ) {

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                    .width(10.dp)
                                    .height(10.dp.limitMin(timeHeight))
                                    .align(Alignment.CenterHorizontally)
                                    .clip(roundedShape)
                                    .background(intervalUI.color.toColor())
                            )

                            AnimatedVisibility(
                                visible = isEditMode,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    "Add Activity",
                                    tint = c.white,
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .size(24.dp)
                                        .clip(roundedShape)
                                        .background(c.blue)
                                        .clickable {
                                            Dialog.show { layer ->
                                                AddIntervalDialogView(
                                                    state = state,
                                                    defaultTime = intervalUI.barTimeFinish,
                                                    onClose = { layer.close() }
                                                )
                                            }
                                        }
                                        .padding(2.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f),
                        ) {

                            if (!intervalUI.isStartsPrevDay) {
                                Text(
                                    text = intervalUI.text,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Start,
                                    fontSize = 14.sp,
                                    color = c.text,
                                )
                            }
                        }
                    }
                }

                item(key = "day__${section.day}") {

                    val headerHeight = 26.dp
                    val headerVerticalPadding = 20.dp

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = headerVerticalPadding)
                            .height(headerHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            section.dayText,
                            modifier = Modifier
                                .fillMaxHeight()
                                .clip(roundedShape)
                                .background(c.blue)
                                .clickable {
                                    Dialog.showDateTimePicker(
                                        defaultTime = UnixTime.byLocalDay(section.day),
                                        minPickableDay = state.minPickerDay,
                                        minSavableDay = state.minPickerDay,
                                        maxDay = UnixTime().localDay,
                                        onSelect = { selectedTime ->
                                            scope.launchEx {
                                                val dayToMove = vm.calcDayToMove(selectedTime.localDay)
                                                val sectionsLater = state.sections.filter { it.day >= dayToMove }
                                                // Header indexes + sum of elements in them
                                                val index = sectionsLater.count() - 1 + sectionsLater.sumOf { it.intervals.size }
                                                val offset = -(listHeight
                                                               - dpToPx((listContentPadding.calculateTopPadding() + listContentPadding.calculateBottomPadding()).value)
                                                               - dpToPx(headerHeight.value)
                                                               - dpToPx(headerVerticalPadding.value)
                                                              )
                                                scrollState.animateScrollToItem(index, offset)
                                            }
                                        }
                                    )
                                }
                                .padding(horizontal = 9.dp)
                                .padding(bottom = 0.5.dp)
                                .wrapContentHeight(),
                            color = c.white,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W400
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.padding(bottom = 22.dp)
        ) {

            val bgColorIsEditModeButton = animateColorAsState(if (isEditMode) c.blue else c.sheetBg)
            val fgColorIsEditModeButton = animateColorAsState(if (isEditMode) c.white else c.textSecondary.copy(alpha = 0.7f))

            Icon(
                painterResource(id = R.drawable.sf_pencil_medium_medium),
                "Edit mode",
                tint = fgColorIsEditModeButton.value,
                modifier = Modifier
                    .padding(start = 28.dp)
                    .size(30.dp)
                    .clip(roundedShape)
                    .background(bgColorIsEditModeButton.value)
                    .clickable {
                        isEditMode = !isEditMode
                    }
                    .padding(8.dp)
            )

            Box(modifier = Modifier.weight(1f))

            Icon(
                Icons.Rounded.Close,
                "Close",
                tint = c.textSecondary.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(end = 28.dp)
                    .size(30.dp)
                    .clip(roundedShape)
                    .background(c.sheetBg)
                    .clickable {
                        onClose()
                    }
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun AddIntervalDialogView(
    state: HistoryVm.State,
    defaultTime: Int,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Box(Modifier.background(c.bg)) {

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 20.dp, bottom = 70.dp)
        ) {

            itemsIndexed(state.activitiesFormAddUI, key = { _, item -> item.activity.id }) { _, activityUI ->

                val isFirst = state.activitiesFormAddUI.first() == activityUI
                MyListView__ItemView(
                    isFirst = isFirst,
                    isLast = state.activitiesFormAddUI.last() == activityUI,
                    withTopDivider = !isFirst,
                ) {
                    MyListView__ItemView__ButtonView(
                        text = activityUI.activity.nameWithEmoji(),
                    ) {
                        Dialog.showDateTimePicker(
                            defaultTime = UnixTime(defaultTime),
                            minPickableDay = 0,
                            minSavableDay = 0,
                            maxDay = UnixTime().localDay,
                            withTimeBtnText = "Save",
                            onSelect = { selectedTime ->
                                activityUI.addInterval(selectedTime) {
                                    scope.launchEx {
                                        onClose()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 21.dp, bottom = 20.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {

            SpacerW1()

            Text(
                "Cancel",
                modifier = Modifier
                    .padding(end = 14.dp)
                    .clip(squircleShape)
                    .clickable {
                        onClose()
                    }
                    .padding(bottom = 5.dp, top = 5.dp, start = 9.dp, end = 9.dp),
                color = c.textSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.W400
            )
        }
    }
}
