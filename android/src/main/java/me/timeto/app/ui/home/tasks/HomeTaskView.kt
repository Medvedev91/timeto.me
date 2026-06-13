package me.timeto.app.ui.home.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.timeto.app.Haptic
import me.timeto.app.R
import me.timeto.app.toColor
import me.timeto.app.ui.HStack
import me.timeto.app.ui.SwipeToAction
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.home.HomeScreen__itemCircleFontSize
import me.timeto.app.ui.home.HomeScreen__itemCircleFontWeight
import me.timeto.app.ui.home.HomeScreen__itemCircleHPadding
import me.timeto.app.ui.home.HomeScreen__itemCircleHeight
import me.timeto.app.ui.home.HomeScreen__itemCircleMarginTrailing
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.home.HomeScreen__primaryFontSize
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.onePx
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.symbol.SymbolView
import me.timeto.app.ui.task_timer.TaskTimerFs
import me.timeto.shared.ActivityUi
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.TaskDb
import me.timeto.shared.vm.home.HomeVm
import me.timeto.shared.vm.home.tasks.HomeTasksItemUi

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeTaskView(
    homeTaskUi: HomeTasksItemUi.HomeTaskUi,
    homeState: HomeVm.State,
) {

    val navigationFs = LocalNavigationFs.current

    val isEditOrDelete = remember { mutableStateOf<Boolean?>(null) }
    val stateOffsetAbsDp = remember { mutableStateOf(0.dp) }

    val scope = rememberCoroutineScope()

    val taskDb: TaskDb =
        homeTaskUi.taskUi.taskDb

    SwipeToAction(
        isStartOrEnd = isEditOrDelete,
        modifier = Modifier
            .height(HomeScreen__itemHeight),
        startView = { state ->
            HomeTaskStaStartView(
                homeTaskUi = homeTaskUi,
                resetSta = { onComplete ->
                    scope.launch {
                        state.reset()
                        onComplete()
                    }
                },
            )
        },
        endView = { state ->
            HomeTaskStaEndView(
                state = state,
                onMoveToTimer = {
                    Haptic.long()
                    homeTaskUi.taskUi.moveToTimer()
                },
                onDelete = {
                    Haptic.long()
                    homeTaskUi.taskUi.delete()
                },
            )
        },
        onStart = {
            true
        },
        onEnd = {
            true
        },
        stateOffsetAbsDp = stateOffsetAbsDp,
        content = {
            HStack(
                modifier = Modifier
                    .height(HomeScreen__itemHeight)
                    .fillMaxWidth()
                    .background(c.black)
                    .padding(horizontal = homeTasksOuterHPadding)
                    .clip(roundedShape)
                    .clickable {
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
                    .padding(start = homeTasksInnerHPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                val timeUi = homeTaskUi.timeUi
                if (timeUi != null) {
                    val bgColor: Color = when (timeUi.status) {
                        TextFeatures.TimeData.STATUS.IN -> c.homeFg
                        TextFeatures.TimeData.STATUS.SOON -> c.blue
                        TextFeatures.TimeData.STATUS.OVERDUE -> c.red
                    }
                    HStack(
                        modifier = Modifier
                            .padding(end = if (homeTaskUi.taskUi.tf.paused != null) 9.dp else HomeScreen__itemCircleMarginTrailing)
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

                if (homeTaskUi.taskUi.tf.paused != null) {
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

                val activityUi: ActivityUi? =
                    homeTaskUi.taskUi.activityUi
                if (activityUi != null) {
                    ZStack(
                        modifier = Modifier
                            .width(HomeScreen__itemCircleHeight),
                    ) {
                        SymbolView(
                            symbol = activityUi.symbol,
                            color = remember(activityUi.colorRgba) {
                                activityUi.colorRgba.toColor()
                            },
                            letterSize = HomeScreen__primaryFontSize,
                            iconSize = 17.dp,
                            emojiSize = HomeScreen__itemCircleFontSize,
                            modifier = Modifier,
                        )
                    }
                }

                Text(
                    text = homeTaskUi.text,
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
                        modifier = Modifier
                            .padding(end = homeTasksInnerHPadding),
                        fontSize = HomeScreen__primaryFontSize,
                        color = noteColor,
                    )
                }

                if ((homeState.taskFolderUi.activityDb != null) &&
                    (taskDb.isToday || taskDb.isTomorrow)
                ) {
                    HomeTasksFolderButton(
                        taskFolderUi = homeTaskUi.taskUi.taskFolderUi,
                        color = if (taskDb.isToday) c.orange else c.indigo,
                        modifier = Modifier
                            .padding(end = 1.dp),
                        onClick = {
                            homeTaskUi.taskUi.updateTaskFolder(
                                taskFolderDb = homeState.taskFolderUi.taskFolderDb,
                            )
                        },
                    )
                }
            }
        },
    )
}
