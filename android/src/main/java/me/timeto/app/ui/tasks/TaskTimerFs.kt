package me.timeto.app.ui.tasks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.*
import me.timeto.app.ui.daytime_picker.DaytimePickerSheet
import me.timeto.app.ui.goals.form.Goal2FormFs
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.navigation.picker.NavigationPickerItem
import me.timeto.app.ui.timer.TimerSheet
import me.timeto.shared.DaytimeUi
import me.timeto.shared.db.TaskDb
import me.timeto.shared.vm.tasks.TaskTimerVm

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskTimerFs(
    taskDb: TaskDb,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (_, state) = rememberVm {
        TaskTimerVm(
            taskDb = taskDb,
        )
    }

    fun showTimerSheet(
        goalUi: TaskTimerVm.GoalUi,
    ) {
        navigationFs.push {
            TimerSheet(
                title = goalUi.text,
                doneTitle = "Start",
                initSeconds = 45 * 60,
                hints = goalUi.goalDb.buildTimerHints(),
                onDone = { timer ->
                    goalUi.start(timer = timer)
                    navigationLayer.close()
                },
            )
        }
    }

    Screen(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxHeight(),
            reverseLayout = true,
        ) {

            val goalsUi = state.goalsUi.reversed()
            goalsUi.forEach { goalUi ->
                item {

                    ZStack(
                        contentAlignment = Alignment.BottomCenter, // for divider
                    ) {

                        HStack(
                            modifier = Modifier
                                .height(42.dp)
                                .combinedClickable(
                                    onClick = {
                                        showTimerSheet(goalUi)
                                    },
                                    onLongClick = {
                                        navigationFs.picker(
                                            title = goalUi.text,
                                            items = goalContextItems,
                                            onDone = { pickerItem ->
                                                when (pickerItem.item) {
                                                    GoalContextItemType.Edit -> {
                                                        navigationFs.push {
                                                            Goal2FormFs(
                                                                goalDb = goalUi.goalDb,
                                                            )
                                                        }
                                                    }

                                                    GoalContextItemType.Timer -> {
                                                        showTimerSheet(goalUi)
                                                    }

                                                    GoalContextItemType.UntilTime -> {
                                                        navigationFs.push {
                                                            DaytimePickerSheet(
                                                                title = "Until Time",
                                                                doneText = "Start",
                                                                daytimeUi = DaytimeUi.now(),
                                                                withRemove = false,
                                                                onDone = { daytimePickerUi ->
                                                                    goalUi.startUntil(daytimeUi = daytimePickerUi)
                                                                    navigationLayer.close()
                                                                },
                                                                onRemove = {},
                                                            )
                                                        }
                                                    }

                                                    GoalContextItemType.RestOfGoal -> {
                                                        goalUi.startRestOfGoal()
                                                        navigationLayer.close()
                                                    }
                                                }
                                            },
                                        )
                                    },
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            Text(
                                text = goalUi.text,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .weight(1f),
                                color = c.text,
                                textAlign = TextAlign.Start,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )

                            HStack(
                                modifier = Modifier
                                    .padding(end = 2.dp),
                            ) {
                                goalUi.timerHintsUi.forEach { timerHintUi ->
                                    Text(
                                        text = timerHintUi.title,
                                        modifier = Modifier
                                            .clip(roundedShape)
                                            .clickable {
                                                timerHintUi.onTap()
                                                navigationLayer.close()
                                            }
                                            .padding(horizontal = 8.dp),
                                        color = c.blue,
                                    )
                                }
                            }
                        }

                        if (goalsUi.first() != goalUi) {
                            Divider(Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

// region Goal Context

private sealed class GoalContextItemType {
    object Edit : GoalContextItemType()
    object Timer : GoalContextItemType()
    object UntilTime : GoalContextItemType()
    object RestOfGoal : GoalContextItemType()
}

private val goalContextItems: List<NavigationPickerItem<GoalContextItemType>> = listOf(
    NavigationPickerItem(
        title = "Edit",
        isSelected = false,
        item = GoalContextItemType.Edit,
    ),
    NavigationPickerItem(
        title = "Timer",
        isSelected = false,
        item = GoalContextItemType.Timer,
    ),
    NavigationPickerItem(
        title = "Until Time",
        isSelected = false,
        item = GoalContextItemType.UntilTime,
    ),
    NavigationPickerItem(
        title = "Rest of Goal",
        isSelected = false,
        item = GoalContextItemType.RestOfGoal,
    ),
)

// endregion
