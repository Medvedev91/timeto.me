package me.timeto.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.UnixTime
import me.timeto.shared.vm.SummarySheetVm

private val barsHeaderHeight = 35.dp
private val hPadding = 8.dp

@Composable
fun SummarySheet(
    layer: WrapperView.Layer,
) {

    val (vm, state) = rememberVm { SummarySheetVm() }

    VStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.sheetBg),
    ) {

        ZStack(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {

            HStack(
                modifier = Modifier
                    .fillMaxSize(),
            ) {

                //
                // Left Part

                ZStack(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp, bottom = 12.dp),
                ) {

                    //
                    // Bars Time Sheet

                    VStack(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = barsHeaderHeight),
                    ) {
                        state.barsTimeRows.forEach { barString ->
                            VStack(
                                modifier = Modifier
                                    .padding(start = hPadding, end = 4.dp)
                                    .weight(1f),
                                verticalArrangement = Arrangement.Bottom,
                            ) {
                                Text(
                                    text = barString,
                                    color = c.textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Light,
                                )
                                SheetDividerFg()
                            }
                        }
                    }

                    //
                    // Bars

                    HStack {

                        val scrollState = rememberLazyListState()

                        LazyRow(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 28.dp),
                            state = scrollState,
                            reverseLayout = true,
                        ) {

                            state.daysIntervalsUi.forEach { dayIntervalsUi ->

                                item {

                                    VStack(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(16.dp),
                                    ) {

                                        ZStack(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(barsHeaderHeight)
                                                .padding(bottom = 8.dp),
                                        ) {

                                            Text(
                                                text = dayIntervalsUi.dayString,
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter),
                                                color = c.textSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Light,
                                                overflow = TextOverflow.Clip,
                                                maxLines = 1,
                                            )
                                        }

                                        VStack(
                                            modifier = Modifier
                                                .padding(start = 4.dp, end = 4.dp)
                                                .clip(roundedShape),
                                        ) {
                                            dayIntervalsUi.intervalsUi.forEach { intervalUi ->
                                                ZStack(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(intervalUi.ratio)
                                                        .background(intervalUi.activityDb?.colorRgba?.toColor() ?: c.sheetFg),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //
                // Right Part

                val activitiesScrollState = rememberScrollState()

                VStack(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = hPadding)
                        .verticalScroll(state = activitiesScrollState),
                ) {

                    state.activitiesUI.forEach { activityUI ->

                        val activityColor = activityUI.activity.colorRgba.toColor()

                        VStack(
                            modifier = Modifier
                                .padding(top = 16.dp),
                        ) {

                            HStack {

                                ActivitySecondaryText(activityUI.perDayString, Modifier.weight(1f))

                                ActivitySecondaryText(activityUI.totalTimeString)
                            }

                            HStack(
                                modifier = Modifier
                                    .padding(top = 4.dp),
                                verticalAlignment = Alignment.Bottom,
                            ) {

                                Text(
                                    text = activityUI.title,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp),
                                    color = c.text,
                                    fontSize = 14.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                )

                                ActivitySecondaryText(activityUI.percentageString)
                            }

                            HStack(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                            ) {

                                ZStack(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(roundedShape)
                                        .background(c.sheetFg),
                                ) {

                                    ZStack(
                                        modifier = Modifier
                                            .fillMaxWidth(activityUI.ratio)
                                            .height(8.dp)
                                            .background(activityColor)
                                            .clip(roundedShape),
                                    )
                                }

                                ZStack(
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .size(8.dp)
                                        .clip(roundedShape)
                                        .background(activityColor)
                                )
                            }
                        }
                    }

                    Padding(vertical = 12.dp)
                }
            }

            if (state.isChartVisible)
                SummaryChartView(state.activitiesUI)
        }

        Sheet__BottomView {

            VStack {

                HStack(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally),
                ) {
                    state.periodHints.forEach { period ->
                        Text(
                            period.title,
                            modifier = Modifier
                                .clip(squircleShape)
                                .clickable {
                                    vm.setPeriod(period.pickerTimeStart, period.pickerTimeFinish)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            fontWeight = if (period.isActive) FontWeight.Black else FontWeight.Light,
                            color = if (period.isActive) c.white else c.text,
                        )
                    }
                }

                HStack(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    Icon(
                        painter = painterResource(R.drawable.sf_chart_pie_small_thin),
                        contentDescription = "Pie Chart",
                        tint = if (state.isChartVisible) c.white else c.textSecondary,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(roundedShape)
                            .background(if (state.isChartVisible) c.blue else c.transparent)
                            .alpha(0.7f)
                            .clickable {
                                vm.toggleIsChartVisible()
                            }
                            .padding(5.dp),
                    )

                    SpacerW1()

                    DateButtonView(
                        text = state.timeStartText,
                        unixTime = state.pickerTimeStart,
                        minTime = state.minPickerTime,
                        maxTime = state.maxPickerTime,
                    ) {
                        vm.setPickerTimeStart(it)
                    }

                    Text(
                        "-",
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, bottom = 1.dp)
                            .align(Alignment.CenterVertically),
                        fontSize = 14.sp,
                        color = c.text
                    )

                    DateButtonView(
                        text = state.timeFinishText,
                        unixTime = state.pickerTimeFinish,
                        minTime = state.minPickerTime,
                        maxTime = state.maxPickerTime,
                    ) {
                        vm.setPickerTimeFinish(it)
                    }

                    SpacerW1()

                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = c.textSecondary,
                        modifier = Modifier
                            .alpha(0.7f)
                            .size(30.dp)
                            .clip(roundedShape)
                            .clickable {
                                layer.close()
                            }
                            .padding(4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivitySecondaryText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        color = c.textSecondary,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Light,
        maxLines = 1,
    )
}

@Composable
private fun DateButtonView(
    text: String,
    unixTime: UnixTime,
    minTime: UnixTime,
    maxTime: UnixTime,
    onSelect: (UnixTime) -> Unit,
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(squircleShape)
            .background(c.summaryDatePicker)
            .clickable {
                Dialog.showDatePicker(
                    unixTime = unixTime,
                    minTime = minTime,
                    maxTime = maxTime,
                    onSelect = onSelect,
                )
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .padding(top = 1.dp),
        color = c.white,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
    )
}
