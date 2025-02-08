package me.timeto.app.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.app.ui.home.HomeTimerView
import me.timeto.app.ui.main.MainTabEnum
import me.timeto.app.ui.main.MainTabsView
import me.timeto.shared.vm.HomeVm

val HomeView__PRIMARY_FONT_SIZE = 16.sp

// MTG - Main Tasks & Goals
val HomeView__MTG_ITEM_HEIGHT = 36.dp
private val mtgCircleHPadding = 6.dp
private val mtgCircleHeight = 22.dp
private val mtgCircleFontSize = 13.sp
private val mtgCircleFontWeight = FontWeight.SemiBold

private val mainTaskHalfHPadding = H_PADDING / 2

@Composable
fun HomeView() {

    val (vm, state) = rememberVm {
        HomeVm()
    }

    val checklistDb = state.checklistDb
    val tab = remember {
        mutableStateOf(MainTabEnum.home)
    }

    VStack(
        modifier = Modifier
            .fillMaxSize()
            .background(c.black)
            .padding(top = (LocalContext.current as MainActivity).statusBarHeightDp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        HomeTimerView(
            vm = vm,
            state = state,
        )

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
                    .zIndex(1f),
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
                        .zIndex(2f),
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
        }

        MainTabsView(
            tab = tab.value,
            onTabChanged = { newTab ->
                tab.value = newTab
            },
        )
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
