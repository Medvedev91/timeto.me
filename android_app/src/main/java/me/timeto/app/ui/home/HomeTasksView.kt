package me.timeto.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.HStack
import me.timeto.app.R
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.onePx
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.activities.timer.ActivitiesTimerFs
import me.timeto.app.ui.activities.timer.ActivityTimerFs
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.shared.TextFeatures
import me.timeto.shared.vm.home.HomeVm

private val mainTaskHalfHPadding: Dp = HomeScreen__hPadding / 2

@Composable
fun HomeTasksView(
    tasks: List<HomeVm.MainTask>,
    modifier: Modifier,
) {

    val navigationFs = LocalNavigationFs.current

    val scrollState = rememberLazyListState()

    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        state = scrollState,
        reverseLayout = true,
    ) {

        items(
            items = tasks,
            key = { it.taskUi.taskDb.id }
        ) { mainTask ->

            HStack(
                modifier = Modifier
                    .height(HomeScreen__itemHeight)
                    .fillMaxWidth()
                    .padding(horizontal = mainTaskHalfHPadding)
                    .clip(roundedShape)
                    .clickable {
                        mainTask.taskUi.taskDb.startIntervalForUi(
                            ifJustStarted = {},
                            ifActivityNeeded = {
                                navigationFs.push {
                                    ActivitiesTimerFs(
                                        strategy = mainTask.timerStrategy,
                                    )
                                }
                            },
                            ifTimerNeeded = { activityDb ->
                                navigationFs.push {
                                    ActivityTimerFs(
                                        activityDb = activityDb,
                                        strategy = mainTask.timerStrategy,
                                    )
                                }
                            },
                        )
                    }
                    .padding(horizontal = mainTaskHalfHPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                val timeUi = mainTask.timeUi
                if (timeUi != null) {
                    val bgColor: Color = when (timeUi.status) {
                        TextFeatures.TimeData.STATUS.IN -> c.homeFg
                        TextFeatures.TimeData.STATUS.SOON -> c.blue
                        TextFeatures.TimeData.STATUS.OVERDUE -> c.red
                    }
                    HStack(
                        modifier = Modifier
                            .padding(end = if (mainTask.taskUi.tf.paused != null) 9.dp else HomeScreen__itemCircleMarginTrailing)
                            .height(HomeScreen__itemCircleHeight)
                            .clip(roundedShape)
                            .background(bgColor)
                            .padding(horizontal = HomeScreen__itemCircleHPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = timeUi.text,
                            modifier = Modifier
                                .padding(top = onePx),
                            fontWeight = HomeScreen__itemCircleFontWeight,
                            fontSize = HomeScreen__itemCircleFontSize,
                            lineHeight = 18.sp,
                            color = c.white,
                        )
                    }
                }

                if (mainTask.taskUi.tf.paused != null) {
                    ZStack(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(HomeScreen__itemCircleHeight)
                            .clip(roundedShape)
                            .background(c.green),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.sf_pause_medium_black),
                            contentDescription = "Paused Task",
                            tint = c.white,
                            modifier = Modifier
                                .size(10.dp),
                        )
                    }
                }

                Text(
                    text = mainTask.text,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .weight(1f),
                    fontSize = HomeScreen__primaryFontSize,
                    color = c.white,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (timeUi != null) {
                    val noteColor: Color = when (timeUi.status) {
                        TextFeatures.TimeData.STATUS.IN -> c.secondaryText
                        TextFeatures.TimeData.STATUS.SOON -> c.blue
                        TextFeatures.TimeData.STATUS.OVERDUE -> c.red
                    }
                    Text(
                        text = timeUi.note,
                        fontSize = HomeScreen__primaryFontSize,
                        color = noteColor,
                    )
                }
            }
        }
    }
}
