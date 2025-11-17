package me.timeto.app.ui.summary

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.toColor
import me.timeto.app.ui.Divider
import me.timeto.app.ui.HStack
import me.timeto.app.ui.Screen
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.roundedShape
import me.timeto.shared.vm.summary.SummaryVm

private val barsHeaderHeight = 35.dp
private val hPadding = 8.dp

@Composable
fun SummaryFs(
    vm: SummaryVm,
    state: SummaryVm.State,
) {

    val mainActivity = LocalActivity.current as MainActivity

    Screen {

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
                        .padding(end = 12.dp, bottom = 56.dp),
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
                                    color = c.secondaryText,
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

                            state.daysBarsUi.forEach { dayBarsUi ->

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
                                                text = dayBarsUi.dayString,
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter),
                                                color = c.secondaryText,
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
                                            dayBarsUi.barsUi.forEach { barUi ->
                                                ZStack(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(barUi.ratio)
                                                        .background(barUi.goalDb?.colorRgba?.toColor() ?: c.gray5),
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

                    state.goalsUi.forEach { goalUi ->
                        GoalView(goalUi)
                    }

                    ZStack(
                        modifier = Modifier
                            .height(56.dp),
                    )
                }
            }

//            if (isChartVisible.value)
//                SummaryChartView(state.goalsUi)
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
        color = c.secondaryText,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Light,
        maxLines = 1,
    )
}

@Composable
private fun GoalView(
    goalUi: SummaryVm.GoalUi,
) {
    val goalColor = goalUi.goalDb.colorRgba.toColor()

    VStack(
        modifier = Modifier
            .padding(top = 16.dp),
    ) {

        HStack {

            ActivitySecondaryText(goalUi.perDayString, Modifier.weight(1f))

            ActivitySecondaryText(goalUi.totalTimeString)
        }

        HStack(
            modifier = Modifier
                .padding(top = 4.dp),
            verticalAlignment = Alignment.Bottom,
        ) {

            Text(
                text = goalUi.title,
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

            ActivitySecondaryText(goalUi.percentageString)
        }

        HStack(
            modifier = Modifier
                .padding(top = 6.dp)
        ) {

            ZStack(
                modifier = Modifier
                    .weight(1f)
                    .clip(roundedShape)
                    .background(c.gray5),
            ) {

                ZStack(
                    modifier = Modifier
                        .fillMaxWidth(goalUi.ratio)
                        .height(8.dp)
                        .background(goalColor)
                        .clip(roundedShape),
                )
            }

            ZStack(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(8.dp)
                    .clip(roundedShape)
                    .background(goalColor)
            )
        }
    }

    if (goalUi.children.isNotEmpty()) {
        HStack(
            modifier = Modifier
                .height(IntrinsicSize.Min), // To use fillMaxHeight() inside
        ) {

            VStack(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .padding(top = 18.dp)
                    .clip(roundedShape)
                    .background(goalColor),
            ) {}

            VStack(
                modifier = Modifier
                    .padding(start = 12.dp),
            ) {
                goalUi.children.forEach { childrenGoalUi ->
                    GoalView(childrenGoalUi)
                }
            }
        }
    }
}
