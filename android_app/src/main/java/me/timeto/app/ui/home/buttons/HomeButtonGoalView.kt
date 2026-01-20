package me.timeto.app.ui.home.buttons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import me.timeto.app.toColor
import me.timeto.app.ui.HStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.daytime_picker.DaytimePickerSheet
import me.timeto.app.ui.goals.form.Goal2FormFs
import me.timeto.app.ui.home.HomeScreen__itemCircleFontSize
import me.timeto.app.ui.home.HomeScreen__itemCircleFontWeight
import me.timeto.app.ui.home.HomeScreen__itemCircleHPadding
import me.timeto.app.ui.home.HomeScreen__itemCircleHeight
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.home.settings.HomeSettingsButtonsFs
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.picker.NavigationPickerItem
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.timer.TimerSheet
import me.timeto.shared.DaytimeUi
import me.timeto.shared.vm.home.buttons.HomeButtonType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeButtonGoalView(
    goal: HomeButtonType.Goal,
) {

    val navigationFs = LocalNavigationFs.current
    val contextPickerItems = remember(goal) {
        buildContextPickerItems(goal)
    }

    HStack(
        modifier = Modifier
            .height(HomeScreen__itemHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        ZStack(
            modifier = Modifier
                .height(HomeScreen__itemCircleHeight)
                .fillMaxWidth()
                .clip(roundedShape)
                .background(c.homeFg)
                .combinedClickable(
                    onClick = {
                        goal.startInterval()
                    },
                    onLongClick = {
                        navigationFs.picker(
                            title = goal.fullText,
                            items = contextPickerItems,
                            onDone = { pickerItem ->
                                when (pickerItem.item) {
                                    ContextPickerItemType.EditGoal -> {
                                        navigationFs.push {
                                            Goal2FormFs(
                                                goalDb = goal.goalDb,
                                            )
                                        }
                                    }

                                    ContextPickerItemType.Timer -> {
                                        navigationFs.push {
                                            TimerSheet(
                                                title = goal.goalTf.textNoFeatures,
                                                doneTitle = "Start",
                                                initSeconds = 45 * 60,
                                                onDone = { newTimerSeconds ->
                                                    goal.startForSeconds(newTimerSeconds)
                                                },
                                            )
                                        }
                                    }

                                    is ContextPickerItemType.TimerHint -> {
                                        pickerItem.item.timerHintUi.onTap()
                                    }

                                    ContextPickerItemType.UntilTime -> {
                                        navigationFs.push {
                                            DaytimePickerSheet(
                                                title = "Until Time",
                                                doneText = "Start",
                                                daytimeUi = DaytimeUi.now(),
                                                withRemove = false,
                                                onDone = { daytimePickerUi ->
                                                    daytimePickerUi.startUntilAsync(goal.goalDb)
                                                },
                                                onRemove = {},
                                            )
                                        }
                                    }

                                    ContextPickerItemType.HomeScreenSettings -> {
                                        navigationFs.push {
                                            HomeSettingsButtonsFs()
                                        }
                                    }
                                }
                            },
                        )
                    },
                ),
        ) {

            ZStack(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(goal.progressRatio)
                    .background(goal.bgColor.toColor())
                    .clip(roundedShape)
                    .align(Alignment.CenterStart),
            )

            Text(
                text = goal.leftText,
                modifier = Modifier
                    .padding(start = HomeScreen__itemCircleHPadding)
                    .align(Alignment.CenterStart),
                color = c.white,
                fontSize = HomeScreen__itemCircleFontSize,
                fontWeight = HomeScreen__itemCircleFontWeight,
                lineHeight = 18.sp,
            )

            Text(
                text = goal.rightText,
                modifier = Modifier
                    .padding(end = HomeScreen__itemCircleHPadding)
                    .align(Alignment.CenterEnd),
                color = c.white,
                fontSize = HomeScreen__itemCircleFontSize,
                fontWeight = HomeScreen__itemCircleFontWeight,
                lineHeight = 18.sp,
            )
        }
    }
}

private fun buildContextPickerItems(
    goal: HomeButtonType.Goal,
): List<NavigationPickerItem<ContextPickerItemType>> {
    val list = mutableListOf<NavigationPickerItem<ContextPickerItemType>>()
    list.add(
        NavigationPickerItem(
            title = "Edit",
            isSelected = false,
            item = ContextPickerItemType.EditGoal,
        )
    )
    list.add(
        NavigationPickerItem(
            title = "Timer",
            isSelected = false,
            item = ContextPickerItemType.Timer,
        )
    )
    goal.timerHintUi.forEach { timerHintUi ->
        list.add(
            NavigationPickerItem(
                title = "    " + timerHintUi.title,
                isSelected = false,
                item = ContextPickerItemType.TimerHint(timerHintUi),
            )
        )
    }
    list.add(
        NavigationPickerItem(
            title = "Until Time",
            isSelected = false,
            item = ContextPickerItemType.UntilTime,
        )
    )
    list.add(
        NavigationPickerItem(
            title = "Home Screen Settings",
            isSelected = false,
            item = ContextPickerItemType.HomeScreenSettings,
        )
    )
    return list
}

private sealed class ContextPickerItemType {
    object EditGoal : ContextPickerItemType()
    object Timer : ContextPickerItemType()

    data class TimerHint(
        val timerHintUi: HomeButtonType.Goal.TimerHintUi,
    ) : ContextPickerItemType()

    object UntilTime : ContextPickerItemType()
    object HomeScreenSettings : ContextPickerItemType()
}
