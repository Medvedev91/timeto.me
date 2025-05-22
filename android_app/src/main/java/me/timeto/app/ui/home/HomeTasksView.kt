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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.HStack
import me.timeto.app.H_PADDING
import me.timeto.app.R
import me.timeto.app.ZStack
import me.timeto.app.c
import me.timeto.app.onePx
import me.timeto.app.roundedShape
import me.timeto.app.toColor
import me.timeto.app.ui.activities.timer.ActivitiesTimerFs
import me.timeto.app.ui.activities.timer.ActivityTimerFs
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.shared.ui.home.HomeVm

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

                val timeUI = mainTask.timeUI
                if (timeUI != null) {
                    HStack(
                        modifier = Modifier
                            .padding(end = if (mainTask.taskUi.tf.paused != null) 9.dp else 8.dp)
                            .height(HomeScreen__itemCircleHeight)
                            .clip(roundedShape)
                            .background(timeUI.textBgColor.toColor())
                            .padding(horizontal = HomeScreen__itemCircleHPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            timeUI.text,
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
                            painterResource(id = R.drawable.sf_pause_medium_black),
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

                if (timeUI != null) {
                    Text(
                        timeUI.note,
                        fontSize = HomeScreen__primaryFontSize,
                        color = timeUI.noteColor.toColor(),
                    )
                }
            }
        }
    }
}

///

private val mainTaskHalfHPadding: Dp = H_PADDING / 2
