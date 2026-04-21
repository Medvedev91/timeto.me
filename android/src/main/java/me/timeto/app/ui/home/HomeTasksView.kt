package me.timeto.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
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
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.task_form.TaskFormFs
import me.timeto.app.ui.tasks.TaskTimerFs
import me.timeto.shared.TextFeatures
import me.timeto.shared.vm.home.HomeVm
import me.timeto.shared.vm.task_form.TaskFormStrategy

private val mainTaskInnerHPadding: Dp = 7.dp
private val mainTaskOuterHPadding: Dp = HomeScreen__hPadding - mainTaskInnerHPadding

@Composable
fun HomeTasksView(
    mainListItemsUi: List<HomeVm.MainListItemUi>,
    modifier: Modifier,
) {
    val scrollState = rememberLazyListState()

    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        state = scrollState,
        reverseLayout = true,
    ) {

        items(
            items = mainListItemsUi,
            key = { it.id },
        ) { itemUi ->
            when (itemUi) {
                is HomeVm.MainListItemUi.MainTaskUi -> TaskView(itemUi)
                is HomeVm.MainListItemUi.TaskFolderBarUi -> TaskFolderBarView(itemUi)
            }
        }
    }
}

@Composable
private fun TaskView(
    taskListUi: HomeVm.MainListItemUi.MainTaskUi,
) {
    val navigationFs = LocalNavigationFs.current

    HStack(
        modifier = Modifier
            .height(HomeScreen__itemHeight)
            .fillMaxWidth()
            .padding(horizontal = mainTaskOuterHPadding)
            .clip(roundedShape)
            .clickable {
                val taskDb = taskListUi.taskUi.taskDb
                taskDb.startIntervalForUi(
                    ifJustStarted = {},
                    ifTimerNeeded = {
                        navigationFs.push {
                            TaskTimerFs(
                                taskDb = taskDb,
                            )
                        }
                    },
                )
            }
            .padding(horizontal = mainTaskInnerHPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        val timeUi = taskListUi.timeUi
        if (timeUi != null) {
            val bgColor: Color = when (timeUi.status) {
                TextFeatures.TimeData.STATUS.IN -> c.homeFg
                TextFeatures.TimeData.STATUS.SOON -> c.blue
                TextFeatures.TimeData.STATUS.OVERDUE -> c.red
            }
            HStack(
                modifier = Modifier
                    .padding(end = if (taskListUi.taskUi.tf.paused != null) 9.dp else HomeScreen__itemCircleMarginTrailing)
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

        if (taskListUi.taskUi.tf.paused != null) {
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
            text = taskListUi.text,
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

@Composable
private fun TaskFolderBarView(
    barUi: HomeVm.MainListItemUi.TaskFolderBarUi,
) {
    val navigationFs = LocalNavigationFs.current

    HStack(
        modifier = Modifier
            .height(HomeScreen__itemHeight)
            .fillMaxWidth()
            .padding(start = mainTaskOuterHPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        HStack(
            modifier = Modifier
                .clip(roundedShape)
                .fillMaxHeight()
                .weight(1f)
                .clickable {
                    navigationFs.push {
                        TaskFormFs(
                            strategy = TaskFormStrategy.NewTask(
                                taskFolderDb = barUi.taskFolderDb,
                            )
                        )
                    }
                }
                .padding(start = mainTaskInnerHPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            ZStack(
                modifier = Modifier
                    .size(HomeScreen__itemCircleHeight)
                    .clip(roundedShape)
                    .background(c.blue),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.sf_plus_medium_bold),
                    contentDescription = "New Task",
                    tint = c.black,
                    modifier = Modifier
                        .size(10.dp),
                )
            }

            Text(
                text = barUi.addButtonText,
                modifier = Modifier
                    .padding(start = 8.dp),
                color = c.blue,
                fontSize = HomeScreen__primaryFontSize,
                maxLines = 1,
            )
        }

        ZStack(
            modifier = Modifier
                .padding(end = 6.dp)
                .size(size = HomeScreen__itemHeight)
                .clip(roundedShape)
                .clickable {
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.sf_house_medium_semibold),
                contentDescription = "New Task",
                tint = c.secondaryText,
                modifier = Modifier
                    .size(23.dp),
            )
        }

        if (barUi.todayTasksCount > 0) {
            ZStack(
                modifier = Modifier
                    .padding(start = 2.dp, end = 8.dp)
                    .size(HomeScreen__itemCircleHeight)
                    .clip(roundedShape)
                    .clickable {
                        barUi.toggleCollapseToday()
                    }
                    .border(2.dp, c.secondaryText, roundedShape),
                contentAlignment = Alignment.Center,
            ) {
                if (barUi.isCollapsed) {
                    Text(
                        text = barUi.todayTasksCount.toString(),
                        modifier = Modifier
                            .padding(start = 1.dp),
                        color = c.secondaryText,
                        fontSize = HomeScreen__itemCircleFontSize,
                        fontWeight = HomeScreen__itemCircleFontWeight,
                        lineHeight = 20.sp,
                    )
                } else {
                    Icon(
                        painterResource(id = R.drawable.sf_chevron_down_medium_bold),
                        contentDescription = "Today Tasks",
                        tint = c.secondaryText,
                        modifier = Modifier
                            .size(10.dp),
                    )
                }
            }
        }
    }
}
