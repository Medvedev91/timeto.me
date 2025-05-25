package me.timeto.app.ui.summary

import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.app.ui.Divider
import me.timeto.app.ui.HStack
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.showDatePicker
import me.timeto.shared.UnixTime
import me.timeto.shared.ui.summary.SummaryVm

private val barsHeaderHeight = 35.dp
private val hPadding = 8.dp

@Composable
fun SummaryFs() {

    val mainActivity = LocalActivity.current as MainActivity
    val navigationLayer = LocalNavigationLayer.current

    val isChartVisible = remember {
        mutableStateOf(false)
    }

    val (vm, state) = rememberVm {
        SummaryVm()
    }

    Screen(
        modifier = Modifier
            .navigationBarsPadding(),
    ) {

        ZStack(
            modifier = Modifier
                .padding(top = mainActivity.statusBarHeightDp)
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
                        .padding(end = 12.dp, bottom = 10.dp),
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
                                Divider()
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
                                                .height(barsHeaderHeight),
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
                        .padding(end = hPadding, bottom = 10.dp)
                        .verticalScroll(state = activitiesScrollState),
                ) {

                    state.activitiesUi.forEach { activityUi ->

                        val activityColor = activityUi.activity.colorRgba.toColor()

                        VStack(
                            modifier = Modifier
                                .padding(top = 16.dp),
                        ) {

                            HStack {

                                ActivitySecondaryText(activityUi.perDayString, Modifier.weight(1f))

                                ActivitySecondaryText(activityUi.totalTimeString)
                            }

                            HStack(
                                modifier = Modifier
                                    .padding(top = 4.dp),
                                verticalAlignment = Alignment.Bottom,
                            ) {

                                Text(
                                    text = activityUi.title,
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

                                ActivitySecondaryText(activityUi.percentageString)
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
                                            .fillMaxWidth(activityUi.ratio)
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
                }
            }

            if (isChartVisible.value)
                SummaryChartView(state.activitiesUi)
        }

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

                FooterIconButton(
                    icon = R.drawable.sf_chart_pie_medium_regular,
                    backgroundColor = if (isChartVisible.value) c.blue else c.transparent,
                    contentDescription = "Pie Chart",
                    onClick = {
                        isChartVisible.value = !isChartVisible.value
                    },
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
                    text = "-",
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 1.dp)
                        .align(Alignment.CenterVertically),
                    fontSize = 14.sp,
                    color = c.text,
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

                FooterIconButton(
                    icon = R.drawable.sf_xmark_circle_medium_regular,
                    backgroundColor = c.transparent,
                    contentDescription = "Close",
                    onClick = {
                        navigationLayer.close()
                    },
                )
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
    val navigationFs = LocalNavigationFs.current
    Text(
        text = text,
        modifier = Modifier
            .clip(squircleShape)
            .background(c.summaryDatePicker)
            .clickable {
                navigationFs.showDatePicker(
                    unixTime = unixTime,
                    minTime = minTime,
                    maxTime = maxTime,
                    onDone = onSelect,
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

@Composable
private fun FooterIconButton(
    @DrawableRes icon: Int,
    backgroundColor: Color,
    contentDescription: String,
    onClick: () -> Unit,
) {

    Icon(
        painter = painterResource(id = icon),
        contentDescription = contentDescription,
        tint = c.textSecondary,
        modifier = Modifier
            .size(30.dp)
            .alpha(0.7f)
            .clip(roundedShape)
            .background(backgroundColor)
            .clickable {
                onClick()
            }
            .padding(4.dp),
    )
}
