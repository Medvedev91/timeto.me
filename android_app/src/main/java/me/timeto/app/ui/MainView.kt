package me.timeto.app.ui

import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandCircleDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.*
import me.timeto.shared.vm.MainVM
import me.timeto.shared.vm.ui.ChecklistStateUI

val bottomNavigationHeight = 56.dp

private val menuIconSize = bottomNavigationHeight
private val menuIconPadding = 14.dp

private val taskCountsHeight = 36.dp

private val mainTaskItemHeight = 32.dp
private val taskListContentPadding = 4.dp

private val menuColor = MainVM.menuColor.toColor()
private val menuTimeColor = MainVM.menuTimeColor.toColor()

private val menuButtonModifier = Modifier.size(menuIconSize).padding(menuIconPadding)

private val tasksTextAnimEnter = fadeIn() + expandVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh))
private val tasksTextAnimExit = fadeOut() + shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh))

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainView() {
    val (vm, state) = rememberVM { MainVM() }

    val checklistUI = state.checklistUI

    Box(
        modifier = Modifier
            .background(c.black)
            .navigationBarsPadding()
    ) {

        Column(
            modifier = Modifier
                .pointerInput(Unit) { }
                .fillMaxSize()
                .padding(top = statusBarHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            val timerColor = animateColorAsState(state.timerData.color.toColor()).value
            val timerButtonsColor = state.timerButtonsColor.toColor()

            Text(
                text = state.title,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .offset(y = 1.dp),
                fontSize = 19.sp,
                fontWeight = FontWeight.Medium,
                color = timerColor,
                textAlign = TextAlign.Center,
            )

            TextFeaturesTriggersView(
                triggers = state.triggers,
                modifier = Modifier.padding(top = 10.dp),
                contentPadding = PaddingValues(horizontal = 50.dp)
            )

            HStack(
                modifier = Modifier
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .offset(x = 4.dp)
                        .clip(squircleShape)
                        .clickable {
                            vm.pauseTask()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(id = R.drawable.sf_pause_medium_thin),
                        contentDescription = "Pause",
                        tint = timerButtonsColor,
                        modifier = Modifier
                            .size(16.dp),
                    )
                }

                Text(
                    text = state.timerData.title,
                    modifier = Modifier
                        .clip(squircleShape)
                        .clickable {
                            vm.toggleIsPurple()
                        }
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    fontSize = run {
                        val len = state.timerData.title.count()
                        when {
                            len <= 5 -> 40.sp
                            len <= 7 -> 35.sp
                            else -> 28.sp
                        }
                    },
                    fontFamily = timerFont,
                    color = timerColor,
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .offset(x = (-2).dp)
                        .clip(squircleShape)
                        .clickable {
                            state.timerData.restart()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.timerData.restartText,
                        modifier = Modifier
                            .padding(bottom = 2.dp),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Thin,
                        color = timerButtonsColor,
                    )
                }
            }

            AnimatedVisibility(
                state.isPurple,
                enter = tasksTextAnimEnter,
                exit = tasksTextAnimExit,
            ) {

                HStack(
                    modifier = Modifier
                        .offset(y = (-4).dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    TimerHintsView(
                        modifier = Modifier,
                        timerHintsUI = state.timerHints,
                        hintHPadding = 10.dp,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Thin,
                        fontColor = timerColor,
                        onStart = {},
                    )

                    Icon(
                        Icons.Rounded.ExpandCircleDown,
                        contentDescription = "More",
                        tint = timerColor,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(28.dp)
                            .clip(roundedShape)
                            .clickable {
                                ActivityTimerSheet__show(
                                    activity = state.activity,
                                    timerContext = state.timerButtonExpandSheetContext,
                                ) {}
                            },
                    )
                }
            }

            ZStack(
                modifier = Modifier
                    .weight(1f),
            ) {

                VStack(
                    modifier = Modifier
                        .padding(bottom = bottomNavigationHeight + taskCountsHeight)
                ) {

                    val checklistScrollState = rememberLazyListState()
                    val mainTasksScrollState = rememberLazyListState()

                    val isMainTasksExists = state.mainTasks.isNotEmpty()

                    if (checklistUI != null) {
                        ChecklistView(
                            checklistUI = checklistUI,
                            modifier = Modifier.weight(1f),
                            scrollState = checklistScrollState,
                        )
                    }

                    MainDivider(
                        calcAlpha = {
                            val isMiddleDividerVisible =
                                (checklistUI != null && (checklistScrollState.canScrollBackward || checklistScrollState.canScrollForward)) ||
                                (isMainTasksExists && (mainTasksScrollState.canScrollBackward || mainTasksScrollState.canScrollForward))
                            if (isMiddleDividerVisible) 1f else 0f
                        }
                    )

                    if (isMainTasksExists) {
                        val mainTasksModifier = if (checklistUI == null)
                            Modifier.weight(1f)
                        else
                            Modifier.height(
                                (taskListContentPadding * 2) +
                                // 4.1f for the smallest emulator
                                (mainTaskItemHeight * state.mainTasks.size.toFloat().limitMax(4.1f))
                            )
                        MainTasksView(
                            tasks = state.mainTasks,
                            modifier = mainTasksModifier,
                            scrollState = mainTasksScrollState,
                        )
                    }

                    if (!isMainTasksExists && checklistUI == null)
                        SpacerW1()
                }

                VStack(
                    modifier = Modifier
                        .padding(bottom = bottomNavigationHeight)
                ) {

                    if (state.isTasksVisible) {

                        ZStack {

                            VStack(
                                modifier = Modifier
                                    .background(c.black),
                            ) {

                                if (checklistUI != null) {
                                    Text(
                                        text = checklistUI.titleToExpand,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                vm.toggleIsTasksVisible()
                                            }
                                            .padding(top = 6.dp, bottom = 12.dp),
                                        textAlign = TextAlign.Center,
                                        color = c.white,
                                    )
                                }

                                MainDivider(calcAlpha = { 1f })

                                BackHandler {
                                    vm.toggleIsTasksVisible()
                                }

                                TasksView(Modifier.weight(1f))
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            colorStops = arrayOf(
                                                0.0f to Color.Transparent,
                                                1f to Color.Black,
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }

        //
        // Navigation

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            verticalAlignment = Alignment.Bottom,
        ) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(squircleShape)
                    .motionEventSpy { event ->
                        if (event.action == MotionEvent.ACTION_DOWN)
                            ActivitiesTimerSheet__show(timerContext = null, withMenu = true)
                    },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Icon(
                    painterResource(id = R.drawable.sf_timer_medium_thin),
                    contentDescription = "Timer",
                    tint = menuColor,
                    modifier = menuButtonModifier,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(squircleShape)
                    .motionEventSpy { event ->
                        if (event.action == MotionEvent.ACTION_DOWN)
                            vm.toggleIsTasksVisible()
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                AnimatedVisibility(
                    !state.isTasksVisible,
                    enter = tasksTextAnimEnter,
                    exit = tasksTextAnimExit,
                ) {
                    Text(
                        text = state.tasksText,
                        modifier = Modifier
                            .height(taskCountsHeight)
                            .padding(top = 3.dp),
                        color = menuColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                    )
                }

                val menuTasksBg = animateColorAsState(if (state.isTasksVisible) c.sheetFg else c.black)

                VStack(
                    modifier = Modifier
                        .height(bottomNavigationHeight)
                        .fillMaxWidth()
                        .clip(squircleShape)
                        .background(menuTasksBg.value),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    Text(
                        text = state.menuTime,
                        color = menuTimeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = timerFont,
                        modifier = Modifier
                            .padding(top = 4.dp, bottom = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .padding(end = 2.dp, bottom = 1.dp)
                            .clip(roundedShape)
                            .background(animateColorAsState(state.batteryBackground.toColor()).value)
                            .padding(start = 4.dp, end = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        val batteryTextColor = state.batteryTextColor.toColor()

                        Icon(
                            painterResource(id = R.drawable.sf_bolt_fill_medium_light),
                            contentDescription = "Battery",
                            tint = batteryTextColor,
                            modifier = Modifier
                                .offset(y = onePx)
                                .size(10.dp)
                        )

                        Text(
                            text = state.batteryText,
                            modifier = Modifier,
                            color = batteryTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(squircleShape)
                    .motionEventSpy { event ->
                        if (event.action == MotionEvent.ACTION_DOWN)
                            Sheet.show { layer ->
                                SettingsSheet(layer = layer)
                            }
                    },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Icon(
                    painterResource(id = R.drawable.sf_ellipsis_circle_medium_thin),
                    contentDescription = "Menu",
                    tint = menuColor,
                    modifier = menuButtonModifier,
                )
            }
        }
    }
}

@Composable
private fun ChecklistView(
    checklistUI: MainVM.ChecklistUI,
    modifier: Modifier,
    scrollState: LazyListState,
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val itemStartPadding = 8.dp
        val checkboxSize = 18.dp
        val checklistItemMinHeight = 44.dp

        val completionState = checklistUI.stateUI
        val checklistMenuInnerIconPadding = (checklistItemMinHeight - checkboxSize) / 2

        MainDivider(
            calcAlpha = {
                if (scrollState.firstVisibleItemIndex > 0) 1f
                else (scrollState.firstVisibleItemScrollOffset.toFloat() * 0.05f).limitMax(1f)
            }
        )

        Row(
            modifier = Modifier
                .padding(
                    start = H_PADDING - itemStartPadding,
                    end = H_PADDING - checklistMenuInnerIconPadding,
                )
                .weight(1f),
        ) {

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = scrollState,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                checklistUI.itemsUI.forEach { itemUI ->

                    item {

                        Row(
                            modifier = Modifier
                                .defaultMinSize(minHeight = checklistItemMinHeight)
                                .fillMaxWidth()
                                .clip(squircleShape)
                                .clickable {
                                    itemUI.toggle()
                                }
                                .padding(start = itemStartPadding),
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
                                    .padding(start = 14.dp),
                                textAlign = TextAlign.Start,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Max)
            ) {

                Column {

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
                            .padding(start = 4.dp)
                            .size(checklistItemMinHeight)
                            .clip(roundedShape)
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
private fun MainTasksView(
    tasks: List<MainVM.MainTask>,
    modifier: Modifier,
    scrollState: LazyListState,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        state = scrollState,
        contentPadding = PaddingValues(vertical = taskListContentPadding),
        reverseLayout = true,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        items(
            items = tasks,
            key = { it.task.id }
        ) { taskItem ->

            Row(
                modifier = Modifier
                    .height(mainTaskItemHeight)
                    .padding(vertical = 3.dp, horizontal = 8.dp)
                    .clip(roundedShape)
                    .clickable {
                        taskItem.task.startIntervalForUI(
                            onStarted = {},
                            activitiesSheet = {
                                ActivitiesTimerSheet__show(taskItem.timerContext, withMenu = false)
                            },
                            timerSheet = { activity ->
                                ActivityTimerSheet__show(
                                    activity = activity,
                                    timerContext = taskItem.timerContext,
                                ) {}
                            },
                        )
                    }
                    .background(
                        color = taskItem.backgroundColor?.toColor() ?: c.transparent,
                        shape = roundedShape
                    )
                    .padding(start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                val type = taskItem.type
                if (type != null) {
                    val iconUI: MainTaskIconUI = when (type) {
                        MainVM.MainTask.Type.event -> MainTaskIconUI(
                            iconRes = R.drawable.sf_calendar_medium_light,
                            modifier = Modifier.padding(start = 1.dp, end = 6.dp).size(15.dp),
                        )
                        MainVM.MainTask.Type.repeating -> MainTaskIconUI(
                            iconRes = R.drawable.sf_repeat_medium_semibold,
                            modifier = Modifier.padding(end = 5.dp).size(15.dp),
                        )
                        MainVM.MainTask.Type.paused -> MainTaskIconUI(
                            iconRes = R.drawable.sf_pause_small_medium,
                            modifier = Modifier.padding(end = 5.dp).size(11.dp),
                        )
                        MainVM.MainTask.Type.important -> MainTaskIconUI(
                            iconRes = R.drawable.sf_flag_fill_medium_regular,
                            modifier = Modifier.padding(start = 2.dp, end = 7.dp).size(11.dp),
                        )
                    }
                    Icon(
                        painterResource(id = iconUI.iconRes),
                        contentDescription = taskItem.text,
                        tint = c.white,
                        modifier = iconUI.modifier,
                    )
                }

                Text(
                    text = taskItem.text,
                    modifier = Modifier.padding(bottom = 1.dp),
                    fontWeight = FontWeight.Normal,
                    fontSize = if (taskItem.backgroundColor == null) 14.sp else 13.sp,
                    color = c.white,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private class MainTaskIconUI(
    @DrawableRes val iconRes: Int,
    val modifier: Modifier,
)

@Composable
private fun MainDivider(
    calcAlpha: () -> Float,
) {
    val alphaAnimate = animateFloatAsState(remember { derivedStateOf(calcAlpha) }.value)
    ZStack(
        modifier = Modifier
            .padding(horizontal = H_PADDING)
            .height(onePx)
            .fillMaxWidth()
            .drawBehind {
                drawRect(color = c.dividerBg.copy(alpha = alphaAnimate.value))
            },
    )
}
