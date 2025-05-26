package me.timeto.app.ui.repeatings.form

import android.widget.NumberPicker
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import me.timeto.app.ui.HStack
import me.timeto.app.H_PADDING
import me.timeto.app.R
import me.timeto.app.ui.VStack
import me.timeto.app.c
import me.timeto.app.dpToPx
import me.timeto.app.isSDKQPlus
import me.timeto.app.onePx
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.button.FormButtonView
import me.timeto.app.ui.form.padding.FormPaddingBottom
import me.timeto.app.ui.form.padding.FormPaddingSectionSection
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.navigation.picker.NavigationPickerItem
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.ui.repeatings.form.RepeatingFormPeriodVm

private val daysOfMonthItemViewItemSize = 36.dp

@Composable
fun RepeatingFormPeriodFs(
    initPeriod: RepeatingDb.Period?,
    onDone: (RepeatingDb.Period) -> Unit,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        RepeatingFormPeriodVm(
            initPeriod = initPeriod,
        )
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = "Done",
                isEnabled = true,
                onClick = {
                    vm.buildSelectedPeriod(
                        dialogsManager = navigationFs,
                        onSuccess = { newPeriod ->
                            onDone(newPeriod)
                            navigationLayer.close()
                        }
                    )
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
        ) {

            item {

                FormPaddingTop()

                FormButton(
                    title = "Type",
                    isFirst = true,
                    isLast = true,
                    note = state.periodNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.picker(
                            items = state.periodPickerItemsUi.map { periodUi ->
                                NavigationPickerItem(
                                    title = periodUi.title,
                                    isSelected = periodUi.idx == state.activePeriodIdx,
                                    item = periodUi.idx,
                                )
                            },
                            onDone = { pickerItem ->
                                vm.setActivePeriodIdx(pickerItem.item)
                            },
                        )
                    },
                )

                FormPaddingSectionSection()

                when (state.activePeriodIdx) {
                    0 -> {}
                    1 -> {
                        VStack(
                            modifier = Modifier
                                .padding(horizontal = H_PADDING + 4.dp),
                        ) {
                            val days: List<Int> = (2..666).toList()
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                factory = { context ->
                                    NumberPicker(context).apply {
                                        setOnValueChangedListener { _, _, new ->
                                            vm.setSelectedNDays(new)
                                        }
                                        displayedValues = days.map { "$it" }.toTypedArray()
                                        if (isSDKQPlus())
                                            textSize = dpToPx(18f).toFloat()
                                        wrapSelectorWheel = false
                                        minValue = 0
                                        maxValue = days.size - 1
                                        value = days.indexOf(state.selectedNDays) // Set last
                                    }
                                }
                            )
                        }
                    }
                    2 -> {
                        val daysOfWeekUi = state.daysOfWeekUi
                        daysOfWeekUi.forEachIndexed { idx, dayOfWeekUi ->
                            FormButtonView(
                                title = dayOfWeekUi.title,
                                titleColor = null,
                                isFirst = idx == 0,
                                isLast = ((idx + 1) == daysOfWeekUi.size),
                                modifier = Modifier,
                                rightView = {
                                    if (dayOfWeekUi.idx in state.selectedDaysOfWeek) {
                                        Icon(
                                            painterResource(R.drawable.sf_checkmark_medium_medium),
                                            contentDescription = "Selected",
                                            tint = c.white,
                                            modifier = Modifier
                                                .padding(end = H_PADDING)
                                                .size(13.dp),
                                        )
                                    }
                                },
                                onClick = {
                                    vm.toggleDayOfWeek(dayOfWeekUi.idx)
                                },
                                onLongClick = null,
                            )
                        }
                    }
                    3 -> {
                        VStack(
                            modifier = Modifier
                                .padding(horizontal = H_PADDING),
                        ) {

                            (1..RepeatingDb.MAX_DAY_OF_MONTH).chunked(7).forEach { days ->
                                HStack {
                                    days.forEachIndexed { idx, day ->
                                        if (idx > 0)
                                            SpacerW1()
                                        val isDaySelected = day in state.selectedDaysOfMonth
                                        DaysOfMonthItemView(
                                            dayName = day.toString(),
                                            isSelected = isDaySelected,
                                        ) {
                                            vm.toggleDayOfMonth(day)
                                        }
                                    }
                                    (0..<(7 - days.size)).forEach { _ ->
                                        Text(
                                            text = "",
                                            modifier = Modifier
                                                .size(daysOfMonthItemViewItemSize),
                                        )
                                        SpacerW1()
                                    }
                                }
                            }

                            val isLastDaySelected: Boolean =
                                RepeatingDb.LAST_DAY_OF_MONTH in state.selectedDaysOfMonth

                            DaysOfMonthItemView(
                                dayName = "Last Day of the Month",
                                isSelected = isLastDaySelected,
                                paddingValues = PaddingValues(
                                    start = 10.dp,
                                    end = 10.dp,
                                    bottom = onePx,
                                ),
                            ) {
                                vm.toggleDayOfMonth(RepeatingDb.LAST_DAY_OF_MONTH)
                            }
                        }
                    }
                    4 -> {

                        state.selectedDaysOfYear.forEachIndexed { idx, day ->
                            FormButton(
                                title = day.longTitle,
                                isFirst = idx == 0,
                                isLast = false,
                                withArrow = true,
                                onClick = {
                                    navigationFs.push {
                                        DayOfTheYearFormFs(
                                            initDay = day,
                                            onDone = { newDay ->
                                                vm.addDayOfTheYear(newDay)
                                            },
                                            onDelete = {
                                                vm.deleteDayOfTheYear(idx = idx)
                                            },
                                        )
                                    }
                                },
                            )
                        }

                        FormButton(
                            title = "New Day",
                            titleColor = c.blue,
                            isFirst = state.selectedDaysOfYear.isEmpty(),
                            isLast = true,
                            onClick = {
                                navigationFs.push {
                                    DayOfTheYearFormFs(
                                        initDay = null,
                                        onDone = { newDay ->
                                            vm.addDayOfTheYear(newDay)
                                        },
                                        onDelete = {},
                                    )
                                }
                            },
                        )
                    }
                    else -> throw Exception()
                }

                FormPaddingBottom(withNavigation = true)
            }
        }
    }
}

@Composable
private fun DaysOfMonthItemView(
    dayName: String,
    isSelected: Boolean,
    paddingValues: PaddingValues = PaddingValues(),
    onClick: () -> Unit,
) {
    val bgColor = animateColorAsState(if (isSelected) c.blue else c.fg)
    Text(
        text = dayName,
        modifier = Modifier
            .padding(bottom = 12.dp)
            .defaultMinSize(
                minWidth = daysOfMonthItemViewItemSize,
                minHeight = daysOfMonthItemViewItemSize,
            )
            .clip(roundedShape)
            .background(bgColor.value)
            .clickable {
                onClick()
            }
            .padding(paddingValues)
            .wrapContentHeight(), // To center vertical
        color = if (isSelected) c.white else c.text,
        textAlign = TextAlign.Center,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun DayOfTheYearFormFs(
    initDay: RepeatingDb.Period.DaysOfYear.MonthDayItem?,
    onDone: (RepeatingDb.Period.DaysOfYear.MonthDayItem) -> Unit,
    onDelete: () -> Unit,
) {
    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val month = remember {
        mutableStateOf(initDay?.monthData ?: RepeatingDb.Period.DaysOfYear.months[0])
    }
    val dayId = remember {
        mutableStateOf(initDay?.dayId ?: 1)
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = "Day of the Year",
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = "Done",
                isEnabled = true,
                onClick = {
                    onDone(
                        RepeatingDb.Period.DaysOfYear.MonthDayItem(
                            monthId = month.value.id,
                            dayId = dayId.value,
                        )
                    )
                    navigationLayer.close()
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
        ) {

            item {

                FormPaddingTop()

                FormButton(
                    title = "Month",
                    isFirst = true,
                    isLast = false,
                    note = month.value.name,
                    withArrow = true,
                    onClick = {
                        val pickerItems = RepeatingDb.Period.DaysOfYear.months.map { monthData ->
                            NavigationPickerItem(
                                title = monthData.name,
                                isSelected = monthData.id == month.value.id,
                                item = monthData,
                            )
                        }
                        navigationFs.picker(
                            items = pickerItems,
                            onDone = { pickerItem ->
                                month.value = pickerItem.item
                                dayId.value = 1
                            },
                        )
                    },
                )

                FormButton(
                    title = "Day",
                    isFirst = false,
                    isLast = true,
                    note = dayId.value.toString(),
                    withArrow = true,
                    onClick = {
                        val pickerItems = month.value.days.map { day ->
                            NavigationPickerItem(
                                title = day.toString(),
                                isSelected = dayId.value == day,
                                item = day,
                            )
                        }
                        navigationFs.picker(
                            items = pickerItems,
                            onDone = { pickerItem ->
                                dayId.value = pickerItem.item
                            },
                        )
                    },
                )

                if (initDay != null) {

                    FormPaddingSectionSection()

                    FormButton(
                        title = "Delete",
                        titleColor = c.red,
                        isFirst = true,
                        isLast = true,
                        onClick = {
                            navigationFs.confirmation("Are you sure?", "Delete") {
                                onDelete()
                                navigationLayer.close()
                            }
                        },
                    )
                }
            }
        }
    }
}
