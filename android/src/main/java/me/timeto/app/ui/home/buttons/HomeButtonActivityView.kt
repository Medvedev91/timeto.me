package me.timeto.app.ui.home.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.R
import me.timeto.app.toColor
import me.timeto.app.ui.HStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.activity_form.ActivityFormFs
import me.timeto.app.ui.c
import me.timeto.app.ui.daytime_picker.DaytimePickerSheet
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
fun HomeButtonActivityView(
    activity: HomeButtonType.Activity,
) {

    val navigationFs = LocalNavigationFs.current
    val contextPickerItems = remember(activity) {
        buildContextPickerItems(activity)
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
                        val isStarted = activity.onBarPressedOrNeedTimerPicker()
                        if (!isStarted) {
                            navigationFs.push {
                                TimerSheet(
                                    title = activity.timerPickerTitle,
                                    doneTitle = "Start",
                                    initSeconds = 45 * 60,
                                    hints = activity.activityDb.buildTimerHints(),
                                    onDone = { newTimerSeconds ->
                                        activity.startForSeconds(newTimerSeconds)
                                    },
                                )
                            }
                        }
                    },
                    onLongClick = {
                        navigationFs.picker(
                            title = activity.fullText,
                            items = contextPickerItems,
                            onDone = { pickerItem ->
                                when (pickerItem.item) {
                                    ContextPickerItemType.EditGoal -> {
                                        navigationFs.push {
                                            ActivityFormFs(
                                                activityDb = activity.activityDb,
                                            )
                                        }
                                    }

                                    ContextPickerItemType.Timer -> {
                                        navigationFs.push {
                                            TimerSheet(
                                                title = activity.timerPickerTitle,
                                                doneTitle = "Start",
                                                initSeconds = 45 * 60,
                                                hints = activity.activityDb.buildTimerHints(),
                                                onDone = { newTimerSeconds ->
                                                    activity.startForSeconds(newTimerSeconds)
                                                },
                                            )
                                        }
                                    }

                                    is ContextPickerItemType.TimerHint -> {
                                        pickerItem.item.timerHintUi.onTap()
                                    }

                                    is ContextPickerItemType.ChildActivity -> {
                                        val childActivityUi = pickerItem.item.childActivityUi
                                        val isStarted = childActivityUi.startOrNeedTimerPicker()
                                        if (!isStarted) {
                                            navigationFs.push {
                                                TimerSheet(
                                                    title = childActivityUi.title,
                                                    doneTitle = "Start",
                                                    initSeconds = 45 * 60,
                                                    hints = childActivityUi.activityDb.buildTimerHints(),
                                                    onDone = { newTimerSeconds ->
                                                        childActivityUi.startForSeconds(newTimerSeconds)
                                                    },
                                                )
                                            }
                                        }
                                    }

                                    ContextPickerItemType.UntilTime -> {
                                        navigationFs.push {
                                            DaytimePickerSheet(
                                                title = "Until Time",
                                                doneText = "Start",
                                                daytimeUi = DaytimeUi.now(),
                                                withRemove = false,
                                                onDone = { daytimePickerUi ->
                                                    daytimePickerUi.startUntilAsync(activity.activityDb)
                                                },
                                                onRemove = {},
                                            )
                                        }
                                    }

                                    is ContextPickerItemType.RestOfGoal -> {
                                        activity.startRestOfGoal()
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

            val goalColor: Color =
                activity.bgColor.toColor()

            val progressRatioAnimate =
                animateFloatAsState(activity.progressRatio)

            ZStack(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressRatioAnimate.value)
                    .background(goalColor)
                    .clip(roundedShape)
                    .align(Alignment.CenterStart),
            )

            HStack(
                modifier = Modifier
                    .align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Text(
                    text = activity.leftText,
                    modifier = Modifier
                        .padding(start = HomeScreen__itemCircleHPadding)
                        .weight(1f),
                    color = c.white,
                    fontSize = HomeScreen__itemCircleFontSize,
                    fontWeight = HomeScreen__itemCircleFontWeight,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    lineHeight = 18.sp,
                )

                if (activity.isCompleted) {
                    CompletedIconView(goalColor)
                } else {
                    Text(
                        text = activity.rightText,
                        modifier = Modifier
                            .padding(end = HomeScreen__itemCircleHPadding),
                        color = c.white,
                        fontSize = HomeScreen__itemCircleFontSize,
                        fontWeight = HomeScreen__itemCircleFontWeight,
                        lineHeight = 18.sp,
                    )
                }
            }
        }
    }
}

private fun buildContextPickerItems(
    activity: HomeButtonType.Activity,
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
    activity.timerHintUi.forEach { timerHintUi ->
        list.add(
            NavigationPickerItem(
                title = "    " + timerHintUi.title,
                isSelected = false,
                item = ContextPickerItemType.TimerHint(timerHintUi),
            )
        )
    }
    activity.childActivitiesUi.forEach { childActivityUi ->
        list.add(
            NavigationPickerItem(
                title = "    " + childActivityUi.title,
                isSelected = false,
                item = ContextPickerItemType.ChildActivity(childActivityUi),
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

    val restOfGoalUi = activity.restOfGoalUi
    if (restOfGoalUi != null) {
        list.add(
            NavigationPickerItem(
                title = restOfGoalUi.title,
                isSelected = false,
                item = ContextPickerItemType.RestOfGoal,
            )
        )
    }
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
        val timerHintUi: HomeButtonType.Activity.TimerHintUi,
    ) : ContextPickerItemType()

    data class ChildActivity(
        val childActivityUi: HomeButtonType.Activity.ChildActivityUi,
    ) : ContextPickerItemType()

    object UntilTime : ContextPickerItemType()
    object RestOfGoal : ContextPickerItemType()
    object HomeScreenSettings : ContextPickerItemType()
}


@Composable
private fun CompletedIconView(
    color: Color,
) {
    ZStack(
        modifier = Modifier
            .size(HomeScreen__itemCircleHeight)
            .padding(3.dp)
            .clip(roundedShape)
            .background(c.white),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(id = R.drawable.sf_checkmark_medium_semibold),
            contentDescription = "Checklist completed",
            tint = color,
            modifier = Modifier
                .size(8.dp),
        )
    }
}
