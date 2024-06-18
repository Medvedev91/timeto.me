package me.timeto.app.ui

import android.view.MotionEvent
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.*
import me.timeto.shared.vm.HomeVM

val HomeView__BOTTOM_NAVIGATION_HEIGHT = 56.dp
val HomeView__PRIMARY_FONT_SIZE = 16.sp

// MTG - Main Tasks & Goals
val HomeView__MTG_ITEM_HEIGHT = 40.dp
private val mtgCircleHPadding = 6.dp
private val mtgCircleHeight = 22.dp
private val mtgCircleFontSize = 13.sp
private val mtgCircleFontWeight = FontWeight.SemiBold

private val mainTasksContentTopPadding = 4.dp
private val mainTaskHalfHPadding = H_PADDING / 2

private val navigationNoteHeight = 38.dp
private val navigationButtonModifier = Modifier.size(HomeView__BOTTOM_NAVIGATION_HEIGHT).padding(14.dp)

private val purpleAnimEnter = fadeIn() + expandVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh))
private val purpleAnimExit = fadeOut() + shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh))

@Composable
fun HomeView() {

    val (vm, state) = rememberVM { HomeVM() }

    val checklistDb = state.checklistDb

    val noteColor = animateColorAsState(state.timerData.noteColor.toColor()).value
    val timerColor = animateColorAsState(state.timerData.timerColor.toColor()).value
    val timerControlsColor = animateColorAsState(state.timerData.controlsColor.toColor()).value

    VStack(
        modifier = Modifier
            .fillMaxSize()
            .background(c.black)
            .padding(top = statusBarHeight)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = state.timerData.note,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .offset(y = 1.dp),
            fontSize = 19.sp,
            fontWeight = FontWeight.Medium,
            color = noteColor,
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
                        state.timerData.togglePomodoro()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(id = R.drawable.sf_pause_medium_thin),
                    contentDescription = "Pause",
                    tint = timerControlsColor,
                    modifier = Modifier
                        .size(16.dp),
                )
            }

            Text(
                text = state.timerData.timerText,
                modifier = Modifier
                    .clip(squircleShape)
                    .clickable {
                        state.timerData.togglePomodoro()
                    }
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                fontSize = run {
                    val len = state.timerData.timerText.count()
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
                        vm.toggleIsPurple()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(id = R.drawable.sf_plus_medium_thin),
                    contentDescription = "Plus",
                    tint = timerControlsColor,
                    modifier = Modifier
                        .size(16.dp),
                )
            }
        }

        AnimatedVisibility(
            state.isPurple,
            enter = purpleAnimEnter,
            exit = purpleAnimExit,
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

        val readmeMessage = state.readmeMessage
        if (readmeMessage != null) {
            MessageButton(
                title = readmeMessage,
                onClick = {
                    vm.onReadmeOpen()
                    Fs.show { layer ->
                        ReadmeSheet(layer)
                    }
                }
            )
        }

        val fdroidMessage = state.fdroidMessage
        if (fdroidMessage != null) {
            MessageButton(
                title = fdroidMessage,
                onClick = {
                    Sheet.show { layer ->
                        FDroidSheet(layer)
                    }
                }
            )
        }

        val whatsNewMessage = state.whatsNewMessage
        if (whatsNewMessage != null) {
            MessageButton(
                title = whatsNewMessage,
                onClick = {
                    Sheet.show { layer ->
                        WhatsNewSheet(layer)
                    }
                }
            )
        }

        ZStack(
            modifier = Modifier
                .weight(1f),
        ) {

            VStack(
                modifier = Modifier
                    .zIndex(1f)
                    .padding(bottom = HomeView__BOTTOM_NAVIGATION_HEIGHT + navigationNoteHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                val checklistScrollState = rememberLazyListState()
                val mainTasksScrollState = rememberLazyListState()

                val isMainTasksExists = state.mainTasks.isNotEmpty()

                if (checklistDb != null) {

                    MainDivider(
                        calcAlpha = {
                            if (checklistScrollState.firstVisibleItemIndex > 0) 1f
                            else (checklistScrollState.firstVisibleItemScrollOffset.toFloat() * 0.05f).limitMax(1f)
                        }
                    )

                    ChecklistView(
                        checklistDb = checklistDb,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 2.dp),
                        scrollState = checklistScrollState,
                        onDelete = {},
                    )
                }

                MainDivider(
                    calcAlpha = {
                        val isMiddleDividerVisible =
                            (checklistDb != null && (checklistScrollState.canScrollBackward || checklistScrollState.canScrollForward)) ||
                                    (isMainTasksExists && (mainTasksScrollState.canScrollBackward || mainTasksScrollState.canScrollForward))
                        if (isMiddleDividerVisible) 1f else 0f
                    }
                )

                if (isMainTasksExists) {
                    val mainTasksModifier = if (checklistDb == null)
                        Modifier.weight(1f)
                    else
                        Modifier.height(
                            mainTasksContentTopPadding +
                                    // 4.5f for the smallest emulator
                                    (HomeView__MTG_ITEM_HEIGHT * state.mainTasks.size.toFloat().limitMax(4.5f))
                        )
                    MainTasksView(
                        tasks = state.mainTasks,
                        modifier = mainTasksModifier,
                        scrollState = mainTasksScrollState,
                    )
                }

                if (!isMainTasksExists && checklistDb == null)
                    SpacerW1()

                state.goalsUI.forEach { goalUi ->

                    HStack(
                        modifier = Modifier
                            .height(HomeView__MTG_ITEM_HEIGHT),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        ZStack(
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .padding(horizontal = H_PADDING)
                                .height(mtgCircleHeight)
                                .fillMaxWidth()
                                .clip(roundedShape)
                                .background(c.homeFg),
                        ) {

                            ZStack(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(goalUi.ratio)
                                    .background(goalUi.bgColor.toColor())
                                    .clip(roundedShape)
                                    .align(Alignment.CenterStart),
                            )

                            Text(
                                text = goalUi.textLeft,
                                modifier = Modifier
                                    .padding(start = mtgCircleHPadding, top = onePx)
                                    .align(Alignment.CenterStart),
                                color = c.white,
                                fontSize = mtgCircleFontSize,
                                fontWeight = mtgCircleFontWeight,
                                lineHeight = 18.sp,
                            )

                            Text(
                                text = goalUi.textRight,
                                modifier = Modifier
                                    .padding(end = mtgCircleHPadding, top = onePx)
                                    .align(Alignment.CenterEnd),
                                color = c.white,
                                fontSize = mtgCircleFontSize,
                                fontWeight = mtgCircleFontWeight,
                                lineHeight = 18.sp,
                            )
                        }
                    }
                }

                Padding(vertical = 8.dp)
            }

            if (state.isTasksVisible) {

                ZStack(
                    modifier = Modifier
                        .zIndex(2f)
                        .padding(bottom = HomeView__BOTTOM_NAVIGATION_HEIGHT),
                ) {

                    TasksView(
                        modifier = Modifier
                            .align(Alignment.CenterEnd),
                        onClose = {
                            vm.toggleIsTasksVisible()
                        },
                    )

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

            NavigationView(
                vm = vm,
                state = state,
                modifier = Modifier
                    .zIndex(0f)
                    .align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun MessageButton(
    title: String,
    onClick: () -> Unit,
) {
    Text(
        text = title,
        modifier = Modifier
            .padding(top = 12.dp)
            .clip(roundedShape)
            .clickable {
                onClick()
            }
            .background(c.red)
            .padding(horizontal = 10.dp)
            .padding(vertical = 4.dp),
        color = c.white,
        fontSize = 14.sp,
    )
}

@Composable
private fun MainTasksView(
    tasks: List<HomeVM.MainTask>,
    modifier: Modifier,
    scrollState: LazyListState,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        state = scrollState,
        contentPadding = PaddingValues(
            top = mainTasksContentTopPadding,
        ),
        reverseLayout = true,
    ) {

        items(
            items = tasks,
            key = { it.task.id }
        ) { mainTask ->

            HStack(
                modifier = Modifier
                    .height(HomeView__MTG_ITEM_HEIGHT)
                    .fillMaxWidth()
                    .padding(horizontal = mainTaskHalfHPadding)
                    .clip(roundedShape)
                    .clickable {
                        mainTask.task.startIntervalForUI(
                            onStarted = {},
                            activitiesSheet = {
                                ActivitiesTimerSheet__show(mainTask.timerContext, withMenu = false)
                            },
                            timerSheet = { activity ->
                                ActivityTimerSheet__show(
                                    activity = activity,
                                    timerContext = mainTask.timerContext,
                                ) {}
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
                            .padding(end = 8.dp)
                            .height(mtgCircleHeight)
                            .clip(roundedShape)
                            .background(timeUI.textBgColor.toColor())
                            .padding(horizontal = mtgCircleHPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            timeUI.text,
                            modifier = Modifier
                                .padding(top = onePx),
                            fontWeight = mtgCircleFontWeight,
                            fontSize = mtgCircleFontSize,
                            lineHeight = 18.sp,
                            color = c.white,
                        )
                    }
                }

                if (mainTask.textFeatures.paused != null) {
                    Icon(
                        painterResource(id = R.drawable.sf_pause_medium_black),
                        contentDescription = "Paused Task",
                        tint = c.homeFontSecondary,
                        modifier = Modifier
                            .padding(end = 5.dp, top = 1.dp + onePx)
                            .size(10.dp),
                    )
                }

                Text(
                    text = mainTask.text,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .weight(1f),
                    fontSize = HomeView__PRIMARY_FONT_SIZE,
                    color = c.white,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (timeUI != null) {
                    Text(
                        timeUI.note,
                        fontSize = HomeView__PRIMARY_FONT_SIZE,
                        color = timeUI.noteColor.toColor(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun NavigationView(
    vm: HomeVM,
    state: HomeVM.State,
    modifier: Modifier,
) {
    Row(
        modifier = modifier
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
                tint = c.homeFontSecondary,
                modifier = navigationButtonModifier,
            )
        }

        VStack(
            modifier = Modifier
                .weight(1f)
                .clip(squircleShape)
                .motionEventSpy { event ->
                    if (event.action == MotionEvent.ACTION_DOWN)
                        vm.toggleIsTasksVisible()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Text(
                text = state.menuNote,
                modifier = Modifier
                    .height(navigationNoteHeight)
                    .padding(top = 8.dp),
                color = c.homeFontSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Light,
            )

            val menuTasksBg = animateColorAsState(if (state.isTasksVisible) c.sheetFg else c.black)

            VStack(
                modifier = Modifier
                    .height(HomeView__BOTTOM_NAVIGATION_HEIGHT)
                    .fillMaxWidth()
                    .clip(squircleShape)
                    .background(menuTasksBg.value),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = state.menuTime,
                    color = c.homeMenuTime,
                    fontSize = 9.sp,
                    lineHeight = 14.sp,
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
                        lineHeight = 14.sp,
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
                        Fs.show { layer ->
                            SettingsSheet(layer = layer)
                        }
                },
            contentAlignment = Alignment.BottomCenter,
        ) {
            Icon(
                painterResource(id = R.drawable.sf_ellipsis_circle_medium_thin),
                contentDescription = "Menu",
                tint = c.homeFontSecondary,
                modifier = navigationButtonModifier,
            )
        }
    }
}

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
