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
import me.timeto.app.ui.activity_form.ActivityFormFs
import me.timeto.app.ui.daytime_picker.DaytimePickerSheet
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
        activityUi: TaskTimerVm.ActivityUi,
    ) {
        navigationFs.push {
            TimerSheet(
                title = activityUi.text,
                doneTitle = "Start",
                initSeconds = 45 * 60,
                hints = activityUi.activityDb.buildTimerHints(),
                onDone = { timer ->
                    activityUi.start(timer = timer)
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

            val activitiesUi = state.activitiesUi.reversed()
            activitiesUi.forEach { activityUi ->
                item {

                    ZStack(
                        contentAlignment = Alignment.BottomCenter, // for divider
                    ) {

                        HStack(
                            modifier = Modifier
                                .height(42.dp)
                                .combinedClickable(
                                    onClick = {
                                        showTimerSheet(activityUi)
                                    },
                                    onLongClick = {
                                        navigationFs.picker(
                                            title = activityUi.text,
                                            items = activityContextItems,
                                            onDone = { pickerItem ->
                                                when (pickerItem.item) {
                                                    ActivityContextItemType.Edit -> {
                                                        navigationFs.push {
                                                            ActivityFormFs(
                                                                activityDb = activityUi.activityDb,
                                                            )
                                                        }
                                                    }

                                                    ActivityContextItemType.Timer -> {
                                                        showTimerSheet(activityUi)
                                                    }

                                                    ActivityContextItemType.UntilTime -> {
                                                        navigationFs.push {
                                                            DaytimePickerSheet(
                                                                title = "Until Time",
                                                                doneText = "Start",
                                                                daytimeUi = DaytimeUi.now(),
                                                                withRemove = false,
                                                                onDone = { daytimePickerUi ->
                                                                    activityUi.startUntil(daytimeUi = daytimePickerUi)
                                                                    navigationLayer.close()
                                                                },
                                                                onRemove = {},
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                        )
                                    },
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            Text(
                                text = activityUi.text,
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
                                activityUi.timerHintsUi.forEach { timerHintUi ->
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

                        if (activitiesUi.first() != activityUi) {
                            Divider(Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

// region Activity Context

private sealed class ActivityContextItemType {
    object Edit : ActivityContextItemType()
    object Timer : ActivityContextItemType()
    object UntilTime : ActivityContextItemType()
}

private val activityContextItems: List<NavigationPickerItem<ActivityContextItemType>> = listOf(
    NavigationPickerItem(
        title = "Edit",
        isSelected = false,
        item = ActivityContextItemType.Edit,
    ),
    NavigationPickerItem(
        title = "Timer",
        isSelected = false,
        item = ActivityContextItemType.Timer,
    ),
    NavigationPickerItem(
        title = "Until Time",
        isSelected = false,
        item = ActivityContextItemType.UntilTime,
    ),
)

// endregion
