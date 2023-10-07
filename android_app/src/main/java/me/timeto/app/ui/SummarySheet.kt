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
import me.timeto.shared.vm.SummarySheetVM

private val periodHintsHeight = 36.dp
private val periodHintShape = SquircleShape(len = 50f)

@Composable
fun SummarySheet(
    layer: WrapperView.Layer,
) {

    val (vm, state) = rememberVM { SummarySheetVM() }

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

                HStack(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                ) {

                    val scrollState = rememberLazyListState()

                    LazyRow(
                        modifier = Modifier
                            .fillMaxSize(),
                        state = scrollState,
                        reverseLayout = true,
                    ) {

                        state.barsUI.forEach { barUI ->
                            item {
                                VStack(
                                    modifier = Modifier
                                        .padding(top = 20.dp, bottom = periodHintsHeight + 4.dp, start = 8.dp)
                                        .width(8.dp)
                                        .fillMaxHeight()
                                        .clip(roundedShape),
                                ) {
                                    barUI.sections.forEach { section ->
                                        ZStack(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(section.ratio)
                                                .background(section.activity?.colorRgba?.toColor() ?: c.sheetFg),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                val activitiesScrollState = rememberScrollState()

                VStack(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .verticalScroll(state = activitiesScrollState),
                ) {

                    state.activitiesUI.forEach { activityUI ->

                        val activityColor = activityUI.activity.colorRgba.toColor()

                        VStack(
                            modifier = Modifier
                                .padding(top = 12.dp),
                        ) {

                            HStack {

                                ActivitySecondaryText(activityUI.perDayString, Modifier.weight(1f))

                                ActivitySecondaryText(activityUI.totalTimeString)
                            }

                            HStack(
                                verticalAlignment = Alignment.Bottom,
                            ) {

                                Text(
                                    text = activityUI.title,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp),
                                    color = c.text,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                )

                                ActivitySecondaryText(activityUI.percentageString)
                            }

                            HStack(
                                modifier = Modifier
                                    .padding(top = 4.dp)
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

                    Padding(height = periodHintsHeight + 16.dp)
                }
            }

            HStack(
                modifier = Modifier
                    .height(periodHintsHeight)
                    .align(Alignment.BottomCenter),
            ) {
                state.periodHints.forEach { period ->
                    Text(
                        period.title,
                        modifier = Modifier
                            .clip(periodHintShape)
                            .clickable {
                                vm.setPeriod(period.pickerTimeStart, period.pickerTimeFinish)
                            }
                            .background(c.sheetBg)
                            .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 7.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = if (period.isActive) FontWeight.Black else FontWeight.Light,
                        color = if (period.isActive) c.white else c.text,
                    )
                }
            }
        }

        Sheet__BottomView {

            HStack(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Icon(
                    painter = painterResource(R.drawable.sf_chart_pie_small_thin),
                    contentDescription = "Pie Chart",
                    tint = c.textSecondary,
                    modifier = Modifier
                        .alpha(0.7f)
                        .size(30.dp)
                        .clip(roundedShape)
                        .clickable {
                            layer.close()
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = c.white,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
    )
}
