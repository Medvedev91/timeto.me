package me.timeto.app.ui

import android.view.MotionEvent
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.rounded.ExpandMore
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
import androidx.compose.ui.unit.Dp
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

private val taskItemHeight = 36.dp
private val taskListContentPadding = 4.dp

private val menuColor = MainVM.menuColor.toColor()
private val menuTimeColor = MainVM.menuTimeColor.toColor()

private val menuButtonModifier = Modifier.size(menuIconSize).padding(menuIconPadding)

private val tasksTextAnimEnter = fadeIn() + expandVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh))
private val tasksTextAnimExit = fadeOut() + shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh))

private val timerButtonsHeight = 34.dp

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

            Text(
                text = state.timerData.title,
                modifier = Modifier
                    .clip(squircleShape)
                    .clickable {
                        vm.toggleIsPurple()
                    }
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                fontSize = 40.sp, // todo compact if long
                fontFamily = timerFont,
                color = timerColor,
            )

            HStack(
                modifier = Modifier
                    .offset(y = (-4).dp)
            ) {

                Icon(
                    painterResource(id = R.drawable.ic_round_pause_24),
                    contentDescription = "Pause",
                    tint = timerColor,
                    modifier = Modifier
                        .size(timerButtonsHeight)
                        .clip(roundedShape)
                        .clickable {
                            vm.pauseTask()
                        }
                        .padding(5.dp),
                )

                Box(
                    modifier = Modifier
                        .height(timerButtonsHeight)
                        .clip(roundedShape)
                        .clickable {
                            state.timerData.restart()
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.timerData.restartText,
                        modifier = Modifier
                            .padding(bottom = 2.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = timerColor,
                    )
                }

                Icon(
                    Icons.Rounded.ExpandMore,
                    contentDescription = "More",
                    tint = timerColor,
                    modifier = Modifier
                        .size(timerButtonsHeight)
                        .clip(roundedShape)
                        .clickable {
                            ActivityTimerSheet__show(
                                activity = state.activity,
                                timerContext = state.timerButtonExpandSheetContext,
                            ) {}
                        },
                )
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
                    val importantTasksScrollState = rememberLazyListState()

                    val isImportantTasksExists = state.importantTasks.isNotEmpty()

                    if (checklistUI != null) {
                        ChecklistView(
                            checklistUI = checklistUI,
                            modifier = Modifier.weight(1f),
                            scrollState = checklistScrollState,
                        )
                    }

                    MainDivider(
                        animateFloatAsState(
                            remember {
                                derivedStateOf {
                                    val isMiddleDividerVisible =
                                        (checklistUI != null && (checklistScrollState.canScrollBackward || checklistScrollState.canScrollForward)) ||
                                        (isImportantTasksExists && (importantTasksScrollState.canScrollBackward || importantTasksScrollState.canScrollForward))
                                    if (isMiddleDividerVisible) 1f else 0f
                                }
                            }.value
                        )
                    )

                    if (isImportantTasksExists) {
                        val importantTasksModifier = if (checklistUI == null)
                            Modifier.weight(1f)
                        else
                            Modifier.height(
                                (taskListContentPadding * 2) +
                                // 4.1f for the smallest emulator
                                (taskItemHeight * state.importantTasks.size.toFloat().limitMax(4.1f))
                            )
                        ImportantTasksView(
                            tasks = state.importantTasks,
                            modifier = importantTasksModifier,
                            scrollState = importantTasksScrollState,
                        )
                    }

                    if (!isImportantTasksExists && checklistUI == null)
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

                                MainDivider(remember { mutableStateOf(1f) })

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
                            .padding(top = 6.dp),
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
                        text = state.timeOfTheDay,
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

        val checklistVContentPadding = 8.dp

        MainDivider(
            animateFloatAsState(
                remember {
                    derivedStateOf { if (scrollState.canScrollBackward) 1f else 0f }
                }.value
            )
        )

        val itemStartPadding = 8.dp
        val checkboxSize = 18.dp
        val checklistItemMinHeight = 44.dp
        val checklistDividerPadding = 14.dp

        val completionState = checklistUI.stateUI
        val checklistMenuInnerIconPadding = (checklistItemMinHeight - checkboxSize) / 2
        val checklistMenuStartIconPadding = 4.dp

        Row(
            modifier = Modifier
                .padding(
                    start = TAB_TASKS_H_PADDING - itemStartPadding,
                    end = TAB_TASKS_H_PADDING - checklistMenuInnerIconPadding,
                )
                .weight(1f),
        ) {

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = scrollState,
                contentPadding = PaddingValues(vertical = checklistVContentPadding),
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
private fun ImportantTasksView(
    tasks: List<MainVM.ImportantTask>,
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
                    .height(taskItemHeight)
                    .padding(vertical = 4.dp, horizontal = 8.dp)
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
                        color = taskItem.borderColor.toColor(),
                        shape = roundedShape
                    )
                    .padding(1.dp)
                    .background(
                        color = taskItem.backgroundColor.toColor(),
                        shape = roundedShape
                    )
                    .padding(start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                val type = taskItem.type
                if (type != null) {
                    val (iconRes: Int, iconSize: Dp) = when (type) {
                        MainVM.ImportantTask.Type.event -> R.drawable.sf_calendar_medium_light to 14.dp
                        MainVM.ImportantTask.Type.repeating -> R.drawable.sf_repeat_medium_semibold to 14.dp
                        MainVM.ImportantTask.Type.paused -> R.drawable.ic_round_pause_24 to 15.dp
                    }
                    Icon(
                        painterResource(id = iconRes),
                        contentDescription = taskItem.text,
                        tint = c.white,
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .size(iconSize),
                    )
                }

                Text(
                    text = taskItem.text,
                    modifier = Modifier.padding(bottom = 1.dp),
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = c.white,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MainDivider(
    alphaAnimate: State<Float>,
) {
    ZStack(
        modifier = Modifier
            .padding(horizontal = TAB_TASKS_H_PADDING)
            .height(onePx)
            .fillMaxWidth()
            .drawBehind {
                drawRect(color = c.dividerBg.copy(alpha = alphaAnimate.value))
            },
    )
}
