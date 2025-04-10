package me.timeto.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import me.timeto.app.HStack
import me.timeto.app.H_PADDING
import me.timeto.app.MainActivity
import me.timeto.app.VStack
import me.timeto.app.ZStack
import me.timeto.app.c
import me.timeto.app.onePx
import me.timeto.app.pxToDp
import me.timeto.app.rememberVm
import me.timeto.app.roundedShape
import me.timeto.app.toColor
import me.timeto.app.ui.checklists.ChecklistView
import me.timeto.app.ui.FDroidSheet
import me.timeto.app.ui.Padding
import me.timeto.app.ui.Sheet
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.TextFeaturesTriggersView
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.readme.ReadmeFs
import me.timeto.app.ui.whats_new.WhatsNewFs
import me.timeto.shared.vm.HomeVm

val HomeScreen__primaryFontSize = 16.sp

val HomeScreen__itemHeight = 36.dp
val HomeScreen__itemCircleHPadding = 6.dp
val HomeScreen__itemCircleHeight = 22.dp
val HomeScreen__itemCircleFontSize = 13.sp
val HomeScreen__itemCircleFontWeight = FontWeight.SemiBold

@Composable
fun HomeScreen() {

    val navigationFs = LocalNavigationFs.current

    val (vm, state) = rememberVm {
        HomeVm()
    }

    val checklistDb = state.checklistDb
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
            contentPadding = PaddingValues(horizontal = 50.dp),
        )

        val readmeMessage = state.readmeMessage
        if (readmeMessage != null) {
            MessageButton(
                title = readmeMessage,
                onClick = {
                    vm.onReadmeOpen()
                    navigationFs.push {
                        ReadmeFs()
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
                    navigationFs.push {
                        WhatsNewFs()
                    }
                }
            )
        }

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
                        maxLines = 1,
                        withAddButton = false,
                        topPadding = 0.dp,
                        bottomPadding = 0.dp,
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
}

///

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
