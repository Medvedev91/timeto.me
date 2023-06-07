package me.timeto.app.ui

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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.*
import me.timeto.shared.vm.FullScreenVM
import me.timeto.shared.vm.ui.ChecklistStateUI

private val dividerColor = AppleColors.gray4Dark.toColor()

private val menuIconSize = bottomNavigationHeight
private val menuIconPadding = 14.dp

private val taskCountsHeight = 36.dp

private val taskItemHeight = 36.dp
private val taskListContentPadding = 4.dp

private val menuColor = FullScreenVM.menuColor.toColor()

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

            val timerData = state.timerData
            val timerSubtitle = timerData.subtitle

            Column(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
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
                        state.cancelTaskText,
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

            AnimatedVisibility(
                timerSubtitle != null && !state.isTabTasksVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {

                Text(
                    text = timerSubtitle ?: "",
                    fontSize = 26.sp,
                    modifier = Modifier
                        .padding(top = 30.dp)
                        .offset(y = 4.dp),
                    fontWeight = FontWeight.Black,
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
                timerSubtitle != null || !state.isCountdown,
                enter = fadeIn() + expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
                exit = fadeOut() + shrinkVertically(),
            ) {

                Text(
                    text = "Restart",
                    color = c.text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .offset(
                            y = animateDpAsState(
                                if (timerSubtitle != null && !state.isTabTasksVisible) (-6).dp else (-10).dp
                            ).value
                        )
                        .clip(RoundedCornerShape(99.dp))
                        .clickable {
                            vm.restart()
                        }
                        .padding(vertical = 8.dp, horizontal = 20.dp),
                )
            }

            ZStack(
                modifier = Modifier
                    .weight(1f)
                    .clipToBounds(),
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

                    FocusDivider(
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

                    // Keep in mind .clipToBounds()

                    AnimatedVisibility(
                        state.isTabTasksVisible,
                        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessHigh)),
                        exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
                    ) {

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
                                                vm.toggleIsTabTasksVisible()
                                            }
                                            .padding(top = 6.dp, bottom = 12.dp),
                                        textAlign = TextAlign.Center,
                                        color = c.white,
                                    )
                                }

                                FocusDivider(remember { mutableStateOf(1f) }, PaddingValues())

                                TabTasksView(
                                    modifier = Modifier.weight(1f),
                                    withRepeatings = false,
                                    onTaskStarted = {
                                        vm.toggleIsTabTasksVisible()
                                    },
                                )
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

            MenuTimerButton(
                contentAlignment = Alignment.BottomCenter,
                onTaskStarted = {},
            )

            val menuTasksBackground = animateColorAsState(
                if (state.isTabTasksVisible) c.gray5 else c.black
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(MySquircleShape())
                    .clickable {
                        vm.toggleIsTabTasksVisible()
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                AnimatedVisibility(
                    !state.isTabTasksVisible,
                    enter = fadeIn() + expandVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh)),
                    exit = fadeOut() + shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh)),
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

                VStack(
                    modifier = Modifier
                        .height(bottomNavigationHeight)
                        .fillMaxWidth()
                        .clip(MySquircleShape())
                        .background(menuTasksBackground.value),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    Text(
                        text = state.timeOfTheDay,
                        color = menuColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Row(
                        modifier = Modifier
                            .padding(end = 2.dp, bottom = 1.dp)
                            .clip(RoundedCornerShape(99.dp))
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

            MenuCloseButton(
                contentAlignment = Alignment.BottomCenter
            ) {
                layer.close()
            }
        }
    }
}

@Composable
private fun RowScope.MenuTimerButton(
    contentAlignment: Alignment,
    onTaskStarted: () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(MySquircleShape())
            .clickable {
                Sheet.show { layer ->
                    ActivitiesTimerSheet(
                        layerTaskSheet = layer,
                        timerContext = null,
                        onTaskStarted = { onTaskStarted() },
                    )
                }
            },
        contentAlignment = contentAlignment,
    ) {
        Icon(
            painterResource(id = R.drawable.sf_timer_medium_thin),
            contentDescription = "Timer",
            tint = menuColor,
            modifier = Modifier
                .size(menuIconSize)
                .padding(menuIconPadding),
        )
    }
}

@Composable
private fun RowScope.MenuCloseButton(
    contentAlignment: Alignment,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(MySquircleShape())
            .clickable {
                onClick()
            },
        contentAlignment = contentAlignment,
    ) {
        Icon(
            painterResource(id = R.drawable.sf_xmark_circle_medium_thin),
            contentDescription = "Close",
            tint = menuColor,
            modifier = Modifier
                .size(menuIconSize)
                .padding(menuIconPadding),
        )
    }
}

@Composable
private fun ChecklistView(
    checklistUI: FullScreenVM.ChecklistUI,
    modifier: Modifier,
    scrollState: LazyListState,
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val checklistVContentPadding = 8.dp

        FocusDivider(
            animateFloatAsState(
                remember {
                    derivedStateOf { if (scrollState.canScrollBackward) 1f else 0f }
                }.value
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .weight(1f),
        ) {

            val checkboxSize = 18.dp
            val checklistItemMinHeight = 42.dp
            val checklistDividerPadding = 14.dp

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
private fun ImportantTasksView(
    tasks: List<FullScreenVM.ImportantTask>,
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
                    .clip(RoundedCornerShape(99.dp))
                    .clickable {
                        taskItem.task.startIntervalForUI(
                            onStarted = {},
                            needSheet = {
                                Sheet.show { layer ->
                                    ActivitiesTimerSheet(
                                        layerTaskSheet = layer,
                                        timerContext = taskItem.timerContext,
                                        onTaskStarted = {},
                                    )
                                }
                            },
                        )
                    }
                    .background(
                        color = taskItem.borderColor.toColor(),
                        shape = RoundedCornerShape(99.dp)
                    )
                    .padding(1.dp)
                    .background(
                        color = taskItem.backgroundColor.toColor(),
                        shape = RoundedCornerShape(99.dp)
                    )
                    .padding(start = 8.dp, end = 8.dp),
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
private fun FocusDivider(
    alphaAnimate: State<Float>,
    padding: PaddingValues = PaddingValues(horizontal = 8.dp),
) {
    ZStack(
        modifier = Modifier
            .padding(padding)
            .height(onePx)
            .fillMaxWidth()
            .drawBehind {
                drawRect(color = dividerColor.copy(alpha = alphaAnimate.value))
            },
    )
}
