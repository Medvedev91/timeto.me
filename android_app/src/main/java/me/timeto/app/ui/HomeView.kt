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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.vm.HomeVm
import me.timeto.shared.vm.ReadmeVm

val HomeView__BOTTOM_NAVIGATION_HEIGHT = 56.dp
val HomeView__PRIMARY_FONT_SIZE = 16.sp

// MTG - Main Tasks & Goals
val HomeView__MTG_ITEM_HEIGHT = 36.dp
private val mtgCircleHPadding = 6.dp
private val mtgCircleHeight = 22.dp
private val mtgCircleFontSize = 13.sp
private val mtgCircleFontWeight = FontWeight.SemiBold

private val mainTaskHalfHPadding = H_PADDING / 2

private val navigationButtonModifier = Modifier.size(HomeView__BOTTOM_NAVIGATION_HEIGHT).padding(14.dp)

private val purpleAnimEnter = fadeIn() + expandVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh))
private val purpleAnimExit = fadeOut() + shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh))

@Composable
fun HomeView() {

    val (vm, state) = rememberVm { HomeVm() }

    val checklistDb = state.checklistDb

    val noteColor = animateColorAsState(state.timerData.noteColor.toColor()).value
    val timerColor = animateColorAsState(state.timerData.timerColor.toColor()).value
    val timerControlsColor = animateColorAsState(state.timerData.controlsColor.toColor()).value

    VStack(
        modifier = Modifier
            .fillMaxSize()
            .background(c.black)
            .padding(top = (LocalContext.current as MainActivity).statusBarHeightDp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        ZStack(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 7.dp),
            contentAlignment = Alignment.TopCenter,
        ) {

            TimerDataNoteText(
                text = state.timerData.note,
                color = noteColor,
            )

            HStack {

                val timerText = state.timerData.timerText
                val timerFontSize: TextUnit = run {
                    val len = timerText.count()
                    when {
                        len <= 5 -> 40.sp
                        len <= 7 -> 35.sp
                        else -> 28.sp
                    }
                }

                VStack(
                    modifier = Modifier
                        .weight(1f)
                        .offset(x = 1.dp),
                ) {

                    TimerDataNoteText(" ", c.transparent)

                    ZStack(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(squircleShape)
                            .clickable {
                                vm.toggleIsPurple()
                            },
                        contentAlignment = Alignment.Center,
                    ) {

                        TimerDataTimerText(" ", timerFontSize, c.transparent)

                        Icon(
                            painterResource(id = R.drawable.sf_info_medium_thin),
                            contentDescription = "Timer Info",
                            tint = timerControlsColor,
                            modifier = Modifier
                                .size(16.dp),
                        )
                    }
                }

                VStack(
                    modifier = Modifier
                        .clip(squircleShape)
                        .clickable {
                            state.timerData.togglePomodoro()
                        },
                ) {

                    TimerDataNoteText(" ", c.transparent)

                    TimerDataTimerText(
                        text = timerText,
                        fontSize = timerFontSize,
                        color = timerColor,
                    )
                }

                VStack(
                    modifier = Modifier
                        .weight(1f)
                        .offset(x = (-1).dp),
                ) {

                    TimerDataNoteText(" ", c.transparent)

                    ZStack(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(squircleShape)
                            .clickable {
                                state.timerData.prolong()
                            },
                        contentAlignment = Alignment.Center,
                    ) {

                        TimerDataTimerText(" ", timerFontSize, c.transparent)

                        val prolongedText = state.timerData.prolongText
                        if (prolongedText != null) {
                            Text(
                                text = prolongedText,
                                color = timerControlsColor,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Thin,
                            )
                        } else {
                            Icon(
                                painterResource(id = R.drawable.sf_plus_medium_thin),
                                contentDescription = "Plus",
                                tint = timerControlsColor,
                                modifier = Modifier
                                    .size(16.dp),
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            state.isPurple,
            enter = purpleAnimEnter,
            exit = purpleAnimExit,
        ) {

            val infoUi = state.timerData.infoUi

            HStack(
                modifier = Modifier
                    .offset(y = (-2).dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                TimerInfoButton(
                    text = infoUi.untilDaytimeUi.text,
                    color = timerColor,
                    onClick = {
                        Sheet.show { layer ->
                            DaytimePickerSheet(
                                layer = layer,
                                title = infoUi.untilPickerTitle,
                                doneText = "Start",
                                daytimeUi = infoUi.untilDaytimeUi,
                                withRemove = false,
                                onPick = { daytimePickerUi ->
                                    infoUi.setUntilDaytime(daytimePickerUi)
                                    vm.toggleIsPurple()
                                },
                                onRemove = {},
                            )
                        }
                    },
                )

                TimerInfoButton(
                    text = infoUi.timerText,
                    color = timerColor,
                    onClick = {
                        ActivityTimerSheet__show(
                            activity = state.activeActivityDb,
                            timerContext = state.timerData.infoUi.timerContext,
                            onStarted = {
                                vm.toggleIsPurple()
                            },
                        )
                    },
                )

                TimerInfoButton(
                    text = "?",
                    color = timerColor,
                    onClick = {
                        ReadmeSheet__show(ReadmeVm.DefaultItem.pomodoro)
                    },
                )
            }
        }

        TextFeaturesTriggersView(
            triggers = state.triggers,
            modifier = Modifier.padding(top = 10.dp),
            contentPadding = PaddingValues(horizontal = 50.dp)
        )

        val readmeMessage = state.readmeMessage
        if (readmeMessage != null) {
            MessageButton(
                title = readmeMessage,
                onClick = {
                    vm.onReadmeOpen()
                    ReadmeSheet__show()
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
                    Fs.show { layer ->
                        WhatsNewFs(layer)
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
                    .padding(bottom = HomeView__BOTTOM_NAVIGATION_HEIGHT),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                //
                // Checklist + Main Tasks

                VStack(
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { coords ->
                            val totalHeight = coords.size.height
                            vm.upListsContainerSize(
                                totalHeight = pxToDp(totalHeight),
                                itemHeight = HomeView__MTG_ITEM_HEIGHT.value,
                            )
                        },
                ) {

                    val checklistScrollState = rememberLazyListState()
                    val mainTasksScrollState = rememberLazyListState()

                    val isMainTasksExists = state.mainTasks.isNotEmpty()
                    val listSizes = state.listsSizes

                    if (checklistDb != null) {
                        ChecklistView(
                            checklistDb = checklistDb,
                            modifier = Modifier
                                .height(listSizes.checklist.dp),
                            scrollState = checklistScrollState,
                            onDelete = {},
                            maxLines = 1,
                        )
                    }

                    if (isMainTasksExists) {
                        MainTasksView(
                            tasks = state.mainTasks,
                            modifier = Modifier
                                .height(listSizes.mainTasks.dp),
                            scrollState = mainTasksScrollState,
                        )
                    }

                    if (!isMainTasksExists && checklistDb == null)
                        SpacerW1()
                }

                state.goalBarsUi.forEach { goalBarUi ->

                    HStack(
                        modifier = Modifier
                            .offset(y = 1.dp)
                            .height(HomeView__MTG_ITEM_HEIGHT),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        ZStack(
                            modifier = Modifier
                                .padding(horizontal = H_PADDING)
                                .height(mtgCircleHeight)
                                .fillMaxWidth()
                                .clip(roundedShape)
                                .background(c.homeFg)
                                .clickable {
                                    goalBarUi.startInterval()
                                },
                        ) {

                            ZStack(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(goalBarUi.ratio)
                                    .background(goalBarUi.bgColor.toColor())
                                    .clip(roundedShape)
                                    .align(Alignment.CenterStart),
                            )

                            Text(
                                text = goalBarUi.textLeft,
                                modifier = Modifier
                                    .padding(start = mtgCircleHPadding, top = onePx)
                                    .align(Alignment.CenterStart),
                                color = c.white,
                                fontSize = mtgCircleFontSize,
                                fontWeight = mtgCircleFontWeight,
                                lineHeight = 18.sp,
                            )

                            Text(
                                text = goalBarUi.textRight,
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
    tasks: List<HomeVm.MainTask>,
    modifier: Modifier,
    scrollState: LazyListState,
) {
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
                    .height(HomeView__MTG_ITEM_HEIGHT)
                    .fillMaxWidth()
                    .padding(horizontal = mainTaskHalfHPadding)
                    .clip(roundedShape)
                    .clickable {
                        mainTask.taskUi.taskDb.startIntervalForUI(
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
                            .padding(end = if (mainTask.taskUi.tf.paused != null) 9.dp else 8.dp)
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

                if (mainTask.taskUi.tf.paused != null) {
                    ZStack(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(mtgCircleHeight)
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
    vm: HomeVm,
    state: HomeVm.State,
    modifier: Modifier,
) {

    HStack(
        modifier = modifier
            .fillMaxWidth()
            .height(HomeView__BOTTOM_NAVIGATION_HEIGHT),
        verticalAlignment = Alignment.Bottom,
    ) {

        ZStack(
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

        val menuTasksBg = animateColorAsState(if (state.isTasksVisible) c.sheetFg else c.black)

        VStack(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(squircleShape)
                .background(menuTasksBg.value)
                .motionEventSpy { event ->
                    if (event.action == MotionEvent.ACTION_DOWN)
                        vm.toggleIsTasksVisible()
                },
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
                    .padding(top = 3.dp, bottom = 5.dp)
            )

            HStack(
                modifier = Modifier
                    .padding(end = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                val batteryUi = state.batteryUi
                val batteryTextColor = animateColorAsState(batteryUi.colorRgba.toColor())

                Icon(
                    painterResource(
                        id = if (batteryUi.isHighlighted)
                            R.drawable.sf_bolt_fill_medium_bold
                        else
                            R.drawable.sf_bolt_fill_medium_light
                    ),
                    contentDescription = "Battery",
                    tint = batteryTextColor.value,
                    modifier = Modifier
                        .offset(y = -halfDpFloor)
                        .size(10.dp)
                )

                Text(
                    text = batteryUi.text,
                    color = batteryTextColor.value,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = if (batteryUi.isHighlighted) FontWeight.Bold else FontWeight.Light,
                )

                Icon(
                    painterResource(id = R.drawable.sf_smallcircle_filled_circle_small_light),
                    contentDescription = "Tasks",
                    tint = c.homeFontSecondary,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(10.dp + halfDpFloor)
                        .offset(y = -halfDpFloor),
                )

                Text(
                    text = state.menuTasksNote,
                    modifier = Modifier
                        .padding(start = 2.dp + halfDpFloor),
                    color = c.homeFontSecondary,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Light,
                )
            }
        }

        ZStack(
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
private fun TimerInfoButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(roundedShape)
            .clickable {
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = color,
        fontSize = 19.sp,
        fontWeight = FontWeight.Thin,
    )
}

///

@Composable
private fun TimerDataNoteText(
    text: String,
    color: Color,
) {
    Text(
        text = text,
        modifier = Modifier
            .padding(bottom = 4.dp)
            .padding(horizontal = H_PADDING),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun TimerDataTimerText(
    text: String,
    fontSize: TextUnit,
    color: Color,
) {
    Text(
        text = text,
        modifier = Modifier
            .padding(vertical = 4.dp),
        fontSize = fontSize,
        fontFamily = timerFont,
        color = color,
    )
}
