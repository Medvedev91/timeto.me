package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import me.timeto.app.*
import me.timeto.app.ui.home.HomeScreen__itemCircleFontSize
import me.timeto.app.ui.home.HomeScreen__itemCircleFontWeight
import me.timeto.app.ui.home.HomeScreen__itemCircleHPadding
import me.timeto.app.ui.home.HomeScreen__itemCircleHeight
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.home.HomeTasksView
import me.timeto.app.ui.home.HomeTimerView
import me.timeto.app.ui.main.MainTabEnum
import me.timeto.app.ui.main.MainTabsView
import me.timeto.shared.vm.HomeVm

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
                                itemHeight = HomeScreen__itemHeight.value,
                            )
                        },
                ) {

                    val checklistScrollState = rememberLazyListState()

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
                        HomeTasksView(
                            tasks = state.mainTasks,
                            modifier = Modifier
                                .height(listSizes.mainTasks.dp),
                        )
                    }

                    if (!isMainTasksExists && checklistDb == null)
                        SpacerW1()
                }

                state.goalBarsUi.forEach { goalBarUi ->

                    HStack(
                        modifier = Modifier
                            .offset(y = 1.dp)
                            .height(HomeScreen__itemHeight),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        ZStack(
                            modifier = Modifier
                                .padding(horizontal = H_PADDING)
                                .height(HomeScreen__itemCircleHeight)
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
                                    .padding(start = HomeScreen__itemCircleHPadding, top = onePx)
                                    .align(Alignment.CenterStart),
                                color = c.white,
                                fontSize = HomeScreen__itemCircleFontSize,
                                fontWeight = HomeScreen__itemCircleFontWeight,
                                lineHeight = 18.sp,
                            )

                            Text(
                                text = goalBarUi.textRight,
                                modifier = Modifier
                                    .padding(end = HomeScreen__itemCircleHPadding, top = onePx)
                                    .align(Alignment.CenterEnd),
                                color = c.white,
                                fontSize = HomeScreen__itemCircleFontSize,
                                fontWeight = HomeScreen__itemCircleFontWeight,
                                lineHeight = 18.sp,
                            )
                        }
                    }
                }

                Padding(vertical = 8.dp)
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
