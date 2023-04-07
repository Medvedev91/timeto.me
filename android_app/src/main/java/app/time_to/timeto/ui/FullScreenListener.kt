package app.time_to.timeto.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.time_to.timeto.*
import app.time_to.timeto.R
import timeto.shared.FullScreenUI
import timeto.shared.onEachExIn
import timeto.shared.vm.FullScreenVM
import timeto.shared.vm.ui.ChecklistStateUI

private val dividerModifier = Modifier.padding(horizontal = 8.dp)
private val dividerColor = Color.White.copy(0.4f)
private val dividerHeight = 1.dp

private val taskItemHeight = 36.dp
private val taskListContentPadding = 4.dp

@Composable
fun FullScreenListener(
    activity: Activity,
    onClose: () -> Unit,
) {
    LaunchedEffect(Unit) {

        FullScreenUI.state.onEachExIn(this) { toOpenOrClose ->

            /**
             * https://developer.android.com/develop/ui/views/layout/immersive#kotlin
             *
             * No systemBars(), because on Redmi the first touch opens navbar.
             *
             * Needs "android:windowLayoutInDisplayCutoutMode shortEdges" in manifest
             * to hide dark space on the top while WindowInsetsCompat.Type.statusBars()
             * like https://stackoverflow.com/q/72179274 in "2. Completely black...".
             * https://developer.android.com/develop/ui/views/layout/display-cutout
             */
            val barTypes = WindowInsetsCompat.Type.statusBars()
            val window = activity.window
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            val flagKeepScreenOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

            ///
            /// Open / Close

            if (!toOpenOrClose) {
                controller.show(barTypes)
                window.clearFlags(flagKeepScreenOn)
                onClose()
                return@onEachExIn
            }

            controller.hide(barTypes)
            window.addFlags(flagKeepScreenOn)
            window.navigationBarColor = Color(0x01000000).toArgb()
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false

            //////

            WrapperView.Layer(
                enterAnimation = fadeIn(spring(stiffness = Spring.StiffnessHigh)),
                exitAnimation = fadeOut(spring(stiffness = Spring.StiffnessHigh)),
                alignment = Alignment.Center,
                onClose = { FullScreenUI.close() },
                content = { layer ->
                    MaterialTheme(colors = myDarkColors()) {
                        FullScreenView(layer)
                    }
                }
            ).show()
        }
    }
}

@Composable
private fun FullScreenView(
    layer: WrapperView.Layer,
) {
    val (vm, state) = rememberVM { FullScreenVM() }

    Box {

        Column(
            modifier = Modifier
                .pointerInput(Unit) { }
                .fillMaxSize()
                .background(c.black)
                .padding(top = statusBarHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Column(
                modifier = Modifier
                    .padding(top = 4.dp, start = 30.dp, end = 30.dp)
                    .offset(y = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = state.title,
                    modifier = Modifier
                        .clip(MySquircleShape())
                        .clickable {
                            vm.toggleIsTaskCancelVisible()
                        }
                        .padding(horizontal = 8.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = c.white,
                    textAlign = TextAlign.Center,
                )

                AnimatedVisibility(
                    state.isTaskCancelVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {

                    Text(
                        "CANCEL",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = c.white,
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 12.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(c.blue)
                            .clickable {
                                vm.cancelTask()
                            }
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }

            TextFeaturesTriggersView(
                triggers = state.triggers,
                modifier = Modifier.padding(top = 10.dp),
                contentPadding = PaddingValues(horizontal = 50.dp)
            )

            val timerData = state.timerData
            AnimatedVisibility(
                timerData.subtitle != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {

                Text(
                    text = timerData.subtitle ?: "",
                    fontSize = 21.sp,
                    modifier = Modifier
                        .padding(top = 36.dp)
                        .offset(y = 3.dp),
                    fontWeight = FontWeight.ExtraBold,
                    color = timerData.subtitleColor.toColor(),
                    letterSpacing = 3.sp,
                )
            }

            Text(
                text = timerData.title,
                modifier = Modifier
                    .clip(MySquircleShape())
                    .clickable {
                        vm.toggleIsCountdown()
                    }
                    .padding(horizontal = 8.dp),
                fontSize = if (timerData.isCompact) 60.sp else 70.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = timerData.titleColor.toColor(),
            )

            AnimatedVisibility(
                timerData.subtitle != null || !state.isCountdown,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier
                    .offset(y = (-4).dp),
            ) {

                Row {

                    val timerIconSize = 52.dp
                    val timerIconPadding = 8.dp

                    Icon(
                        painterResource(id = R.drawable.sf_timer_large_light),
                        contentDescription = "Timer",
                        tint = c.white,
                        modifier = Modifier
                            .size(timerIconSize)
                            .clip(RoundedCornerShape(99.dp))
                            .clickable {
                                Sheet.show { layer ->
                                    ActivityTimerSheet(
                                        layer = layer,
                                        activity = state.activity,
                                        timerContext = state.activityTimerContext,
                                    )
                                }
                            }
                            .padding(timerIconPadding)
                            .padding(top = 3.dp),
                    )

                    Icon(
                        painterResource(id = R.drawable.sf_arrow_counterclockwise_large_light),
                        contentDescription = "Restart",
                        tint = c.white,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(timerIconSize)
                            .clip(RoundedCornerShape(99.dp))
                            .clickable {
                                vm.restart()
                            }
                            .padding(timerIconPadding),
                    )
                }
            }

            val checklistUI = state.checklistUI
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clipToBounds(), // https://stackoverflow.com/a/71527654
                contentAlignment = Alignment.BottomCenter,
            ) {

                ///
                /// Compact Tasks Mode

                Column(
                    modifier = Modifier.fillMaxHeight(),
                ) {

                    val checklistScrollState = rememberLazyListState()

                    AnimatedVisibility(
                        visible = !state.isTaskListShowed,
                        modifier = Modifier.weight(1f),
                        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
                        exit = fadeOut(spring(stiffness = Spring.StiffnessHigh)),
                    ) {
                        if (checklistUI != null) {
                            ExpandedChecklist(
                                checklistScrollState = checklistScrollState,
                                checklistUI = checklistUI,
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = !state.isTaskListShowed,
                        modifier = Modifier.height(
                            taskItemHeight * state.tasksImportant.size
                            + dividerHeight
                            + taskListContentPadding * 2
                        ),
                        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
                        exit = fadeOut(spring(stiffness = Spring.StiffnessHigh)),
                    ) {

                        val taskListScrollState = rememberLazyListState()
                        val isNavDividerVisible = state.checklistUI != null &&
                                                  (checklistScrollState.canScrollBackward || checklistScrollState.canScrollForward)

                        TaskList(
                            taskListScrollState = taskListScrollState,
                            isNavDividerVisible = isNavDividerVisible,
                            tasks = state.tasksImportant,
                        )
                    }
                }

                ///
                /// Full Tasks Mode

                Column {

                    if (checklistUI != null) {

                        Column(
                            // Fixed height to fix task list twitching while hiding
                            modifier = Modifier
                                .height(60.dp),
                        ) {

                            AnimatedVisibility(
                                visible = state.isTaskListShowed,
                                enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
                                exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)),
                            ) {

                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth()
                                        .clickable {
                                            vm.toggleIsCompactTaskList()
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {

                                    Text(
                                        text = checklistUI.collapsedTitle,
                                        color = c.white,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                    )

                                    Icon(
                                        painterResource(R.drawable.sf_chevron_compact_down_medium_thin),
                                        contentDescription = "Expand Checklist",
                                        tint = c.white,
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .height(5.dp),
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = state.isTaskListShowed,
                        modifier = Modifier.weight(1f),
                        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
                                slideInVertically(
                                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                    initialOffsetY = { it }
                                ),
                        exit = fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) +
                               slideOutVertically(
                                   animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                   targetOffsetY = { it + 20 }
                               ),
                    ) {

                        val taskListScrollState = rememberLazyListState()
                        val isNavDividerVisible =
                            (checklistUI != null) ||
                            (taskListScrollState.canScrollBackward || taskListScrollState.canScrollForward)

                        TaskList(
                            taskListScrollState = taskListScrollState,
                            isNavDividerVisible = isNavDividerVisible,
                            tasks = state.tasksAll,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                val menuIconSize = 58.dp
                val menuIconAlpha = 0.5f
                val menuIconPadding = 15.dp

                Icon(
                    painterResource(id = R.drawable.sf_pencil_circle_medimu_thin),
                    contentDescription = "Menu",
                    tint = c.white,
                    modifier = Modifier
                        .weight(1f)
                        .alpha(menuIconAlpha)
                        .clip(MySquircleShape())
                        .size(menuIconSize)
                        .clickable {
                            Sheet.show { layer ->
                                TaskFormSheet(
                                    task = null,
                                    layer = layer,
                                )
                            }
                        }
                        .padding(menuIconPadding),
                )

                Column(
                    modifier = Modifier
                        .offset(y = (-2).dp)
                        .align(Alignment.Top)
                        .clip(MySquircleShape())
                        .clickable {
                            vm.toggleIsCompactTaskList()
                        }
                        .padding(top = 6.dp, bottom = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    Icon(
                        painterResource(
                            id = if (!state.isTaskListShowed) R.drawable.sf_chevron_compact_up_medium_thin
                            else R.drawable.sf_chevron_compact_down_medium_thin
                        ),
                        contentDescription = if (!state.isTaskListShowed) "Show All Tasks" else "Hide All Tasks",
                        tint = c.white,
                        modifier = Modifier
                            .padding(bottom = 3.dp)
                            .alpha(menuIconAlpha)
                            .size(width = 20.dp, height = 6.dp)
                    )

                    val batteryBackground = state.batteryBackground
                    val batteryBackgroundAnimation = animateColorAsState(
                        batteryBackground?.toColor() ?: c.transparent
                    )

                    Text(
                        text = state.timeOfTheDay,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .alpha(menuIconAlpha),
                        color = c.white,
                        fontSize = 14.sp,
                    )

                    Row(
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .clip(RoundedCornerShape(99.dp))
                            // First alpha for background, then for content
                            .background(batteryBackgroundAnimation.value)
                            .alpha(if (batteryBackground != null) 0.9f else menuIconAlpha)
                            //
                            .padding(start = 4.dp, end = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        Icon(
                            painterResource(id = R.drawable.sf_bolt_fill_medium_light),
                            contentDescription = "Battery",
                            tint = c.white,
                            modifier = Modifier
                                .size(10.dp)
                        )

                        Text(
                            text = state.battery,
                            modifier = Modifier,
                            color = c.white,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                        )
                    }
                }

                Icon(
                    painterResource(id = R.drawable.sf_xmark_circle_medium_thin),
                    contentDescription = "Close",
                    tint = c.white,
                    modifier = Modifier
                        .weight(1f)
                        .alpha(menuIconAlpha)
                        .clip(MySquircleShape())
                        .size(menuIconSize)
                        .clickable {
                            layer.close()
                        }
                        .padding(menuIconPadding),
                )
            }
        }
    }
}

@Composable
private fun ExpandedChecklist(
    checklistScrollState: LazyListState,
    checklistUI: FullScreenVM.ChecklistUI,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = 8.dp)
    ) {

        val checklistVContentPadding = 12.dp

        Divider(
            modifier = dividerModifier,
            color = animateColorAsState(
                if (checklistScrollState.canScrollBackward) dividerColor else c.transparent,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
            ).value
        )

        Row(
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .weight(1f),
        ) {

            val checkboxSize = 18.dp
            val checklistItemMinHeight = 42.dp
            val checklistDividerPadding = 14.dp

            LazyColumn(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = checklistVContentPadding),
                state = checklistScrollState,
            ) {

                checklistUI.itemsUI.forEach { itemUI ->

                    item {

                        Row(
                            modifier = Modifier
                                .defaultMinSize(minHeight = checklistItemMinHeight)
                                .fillMaxWidth()
                                .clip(MySquircleShape())
                                .clickable {
                                    itemUI.toggle()
                                }
                                .padding(start = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {

                            Icon(
                                painterResource(
                                    id = if (itemUI.item.isChecked)
                                        R.drawable.sf_checkmark_square_fill_medium_regular
                                    else
                                        R.drawable.sf_square_medium_regular
                                ),
                                contentDescription = "Checkbox",
                                tint = c.white,
                                modifier = Modifier
                                    .size(checkboxSize),
                            )

                            Text(
                                text = itemUI.item.text,
                                color = c.white,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .padding(start = checklistDividerPadding),
                                textAlign = TextAlign.Start,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(top = checklistVContentPadding)
                    .height(IntrinsicSize.Max)
            ) {

                Box(
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .alpha(.5f)
                        .background(c.white)
                        .width(1.dp)
                        .fillMaxHeight(),
                )

                Column {

                    val completionState = checklistUI.stateUI
                    val checklistMenuInnerIconPadding = (checklistItemMinHeight - checkboxSize) / 2
                    val checklistMenuStartIconPadding = 4.dp
                    Icon(
                        painterResource(
                            id = when (completionState) {
                                is ChecklistStateUI.Completed -> R.drawable.sf_checkmark_square_fill_medium_regular
                                is ChecklistStateUI.Empty -> R.drawable.sf_square_medium_regular
                                is ChecklistStateUI.Partial -> R.drawable.sf_minus_square_fill_medium_medium
                            }
                        ),
                        contentDescription = completionState.actionDesc,
                        tint = c.white,
                        modifier = Modifier
                            .padding(start = checklistMenuStartIconPadding)
                            .size(checklistItemMinHeight)
                            .clip(RoundedCornerShape(99.dp))
                            .clickable {
                                completionState.onClick()
                            }
                            .padding(checklistMenuInnerIconPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskList(
    taskListScrollState: LazyListState,
    isNavDividerVisible: Boolean,
    tasks: List<FullScreenVM.TaskListItem>,
) {

    Column(
        modifier = Modifier
            .fillMaxHeight()
    ) {

        Divider(
            modifier = dividerModifier,
            color = if (isNavDividerVisible) dividerColor else c.transparent,
            thickness = dividerHeight,
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            reverseLayout = true,
            state = taskListScrollState,
            contentPadding = PaddingValues(vertical = taskListContentPadding),
        ) {

            items(
                items = tasks,
                key = { it.id }
            ) { taskItem ->

                when (taskItem) {

                    is FullScreenVM.TaskListItem.ImportantTask -> {
                        Row(
                            modifier = Modifier
                                .height(taskItemHeight)
                                .clip(MySquircleShape())
                                .clickable {
                                    taskItem.task.startIntervalForUI(
                                        onStarted = {},
                                        needSheet = {
                                            Sheet.show { layer ->
                                                TaskSheet(layer, taskItem.task)
                                            }
                                        },
                                    )
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .clip(MySquircleShape(len = 30f))
                                    .background(taskItem.backgroundColor.toColor())
                                    .padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.sf_calendar_medium_light),
                                    contentDescription = "Event",
                                    tint = c.white,
                                    modifier = Modifier
                                        .padding(end = 5.dp)
                                        .size(14.dp),
                                )
                                Text(
                                    text = taskItem.text,
                                    fontWeight = FontWeight.Light,
                                    fontSize = 12.sp,
                                    color = c.white,
                                )
                            }
                        }
                    }

                    is FullScreenVM.TaskListItem.RegularTask -> {
                        Row(
                            modifier = Modifier
                                .height(taskItemHeight)
                                .clip(MySquircleShape())
                                .clickable {
                                    taskItem.task.startIntervalForUI(
                                        onStarted = {},
                                        needSheet = {
                                            Sheet.show { layer ->
                                                TaskSheet(layer, taskItem.task)
                                            }
                                        },
                                    )
                                }
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            Text(
                                text = taskItem.text,
                                color = taskItem.textColor.toColor(),
                                fontWeight = FontWeight.Light,
                                fontSize = 13.sp,
                            )
                        }
                    }

                    is FullScreenVM.TaskListItem.NoTasksText -> {
                        Text(
                            text = taskItem.text,
                            modifier = Modifier
                                .height(taskItemHeight),
                            color = c.textSecondary,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}
