package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.app.ui.activities.timer.ActivitiesTimerFs
import me.timeto.app.ui.activities.timer.ActivityTimerFs
import me.timeto.app.ui.events.EventFormFs
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.tasks.form.TaskFormFs
import me.timeto.app.ui.tasks.tab.TasksTabDragItem
import me.timeto.app.ui.tasks.tab.TasksTabDropItem
import me.timeto.app.ui.tasks.tab.TasksTabView__LIST_SECTION_PADDING
import me.timeto.app.ui.tasks.tab.TasksTabView__PADDING_END
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.launchEx
import me.timeto.shared.ui.tasks.form.TaskFormStrategy
import me.timeto.shared.ui.tasks.tab.tasks.TasksTabTasksVm

private val inputShape = SquircleShape(16.dp)
private val highlightTimeShape = SquircleShape(8.dp)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TasksListView(
    taskFolderDb: TaskFolderDb,
    dragItem: MutableState<TasksTabDragItem?>,
) {

    val navigationFs = LocalNavigationFs.current

    val (vm, state) = rememberVm(taskFolderDb) {
        TasksTabTasksVm(taskFolderDb)
    }
    val tmrwUi = state.tmrwUi

    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight(),
        reverseLayout = true,
        contentPadding = PaddingValues(
            top = TasksTabView__LIST_SECTION_PADDING,
            end = TasksTabView__PADDING_END
        ),
        state = listState,
    ) {

        item {

            Column(
                modifier = Modifier
                    .pointerInput(Unit) { } // Ignore clicks through
                    .padding(
                        top = TasksTabView__LIST_SECTION_PADDING,
                        bottom = TasksTabView__LIST_SECTION_PADDING,
                    )
            ) {

                Row(
                    modifier = Modifier
                        .padding(start = H_PADDING - 2.dp)
                        .border(width = onePx, color = c.dividerBg, shape = inputShape)
                        .height(IntrinsicSize.Min) // To use fillMaxHeight() inside
                        .clickable {
                            navigationFs.push {
                                TaskFormFs(
                                    strategy = TaskFormStrategy.NewTask(
                                        taskFolderDb = taskFolderDb,
                                    )
                                )
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    ZStack(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 42.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = "Task",
                            modifier = Modifier
                                .padding(start = 14.dp),
                            color = c.text.copy(alpha = 0.5f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp, bottom = 4.dp, end = 4.dp)
                            .fillMaxHeight()
                            .clip(squircleShape)
                            .background(c.blue)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            "SAVE",
                            color = c.white,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W600,
                        )
                    }
                }
            }
        }

        if (tmrwUi != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = TasksTabView__LIST_SECTION_PADDING),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        tmrwUi.curTimeString,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W300,
                        color = c.textSecondary,
                    )
                }
            }
        }

        val tasksVmUi = state.tasksVmUi
        items(
            tasksVmUi,
            key = { it.taskUi.taskDb.id }
        ) { taskVmUi ->
            val isFirst = taskVmUi == tasksVmUi.firstOrNull()

            Box(
                modifier = Modifier
                    .background(c.bg)
                // .animateItemPlacement() // Slow rendering on IME open
            ) {

                val ignoreOneSwipeToAction = remember { mutableStateOf(false) }
                val isEditOrDelete = remember { mutableStateOf<Boolean?>(null) }
                val stateOffsetAbsDp = remember { mutableStateOf(0.dp) }

                val localDragItem = remember(tasksVmUi) {
                    TasksTabDragItem(
                        mutableStateOf(null),
                        { drop ->
                            when (drop) {
                                is TasksTabDropItem.Calendar -> true
                                is TasksTabDropItem.Folder -> drop.taskFolderDb.id != taskVmUi.taskUi.taskDb.folder_id
                            }
                        }
                    ) { drop ->
                        // Otherwise, the mod of editing is activated, so the keyboard is started.
                        // And since the task is transferred, sometimes just opens the keyboard.
                        ignoreOneSwipeToAction.value = true
                        scope.launchEx {
                            when (drop) {
                                is TasksTabDropItem.Calendar -> {
                                    vibrateShort()
                                    navigationFs.push {
                                        EventFormFs(
                                            initEventDb = null,
                                            initText = taskVmUi.taskUi.taskDb.text,
                                            initTime = null,
                                            onDone = {
                                                taskVmUi.delete()
                                            },
                                        )
                                    }
                                }
                                is TasksTabDropItem.Folder -> {
                                    vibrateLong()
                                    taskVmUi.upFolder(drop.taskFolderDb)
                                }
                            }
                        }
                    }
                }

                LaunchedEffect(isEditOrDelete.value, stateOffsetAbsDp.value) {
                    dragItem.value = if (isEditOrDelete.value == true && stateOffsetAbsDp.value > 10.dp)
                        localDragItem
                    else null
                }
                DisposableEffect(Unit) {
                    onDispose {
                        dragItem.value = null
                    }
                }

                SwipeToAction(
                    isStartOrEnd = isEditOrDelete,
                    ignoreOneAction = ignoreOneSwipeToAction,
                    startView = {
                        val dropItem = localDragItem.focusedDrop.value
                        SwipeToAction__StartView(
                            text = if (dropItem != null) "Move to ${dropItem.name}" else "Edit",
                            bgColor = if (dropItem != null) c.tasksDropFocused else c.blue
                        )
                    },
                    endView = { state ->
                        SwipeToAction__DeleteView(state, taskVmUi.taskUi.taskDb.text) {
                            vibrateLong()
                            taskVmUi.delete()
                        }
                    },
                    onStart = {
                        navigationFs.push {
                            TaskFormFs(
                                strategy = TaskFormStrategy.EditTask(
                                    taskDb = taskVmUi.taskUi.taskDb,
                                ),
                            )
                        }
                        false
                    },
                    onEnd = {
                        true
                    },
                    toVibrateStartEnd = listOf(true, false),
                    stateOffsetAbsDp = stateOffsetAbsDp,
                ) {

                    Box(
                        modifier = Modifier
                            .background(c.bg)
                            .clickable {
                                taskVmUi.taskUi.taskDb.startIntervalForUi(
                                    ifJustStarted = {},
                                    ifActivityNeeded = {
                                        navigationFs.push {
                                            ActivitiesTimerFs(
                                                strategy = taskVmUi.timerStrategy,
                                            )
                                        }
                                    },
                                    ifTimerNeeded = { activityDb ->
                                        navigationFs.push {
                                            ActivityTimerFs(
                                                activityDb = activityDb,
                                                strategy = taskVmUi.timerStrategy,
                                            )
                                        }
                                    },
                                )
                            }
                            .padding(start = H_PADDING),
                        contentAlignment = Alignment.BottomCenter
                    ) {

                        Column(
                            modifier = Modifier
                                .padding(top = 10.dp, bottom = 10.dp),
                            verticalArrangement = Arrangement.Center
                        ) {

                            val vPadding = 3.dp

                            val timeUi = taskVmUi.timeUi
                            if (timeUi != null) {
                                Row(
                                    modifier = Modifier
                                        .padding(bottom = vPadding),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {

                                    when (timeUi) {

                                        is TasksTabTasksVm.TaskVmUi.TimeUi.HighlightUi -> {
                                            Row(
                                                modifier = Modifier
                                                    .offset(x = (-1).dp)
                                                    .clip(highlightTimeShape)
                                                    .background(timeUi.backgroundColor.toColor())
                                                    .padding(start = 5.dp, end = 4.dp, top = 3.dp, bottom = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {

                                                when (timeUi.timeData.type) {

                                                    TextFeatures.TimeData.TYPE.EVENT -> {
                                                        Icon(
                                                            painterResource(id = R.drawable.sf_calendar_medium_light),
                                                            contentDescription = "Event",
                                                            tint = c.white,
                                                            modifier = Modifier
                                                                .padding(end = 5.dp)
                                                                .size(14.dp),
                                                        )
                                                    }

                                                    TextFeatures.TimeData.TYPE.REPEATING -> {
                                                        Icon(
                                                            painterResource(id = R.drawable.sf_repeat_medium_semibold),
                                                            contentDescription = "Repeating",
                                                            tint = c.white,
                                                            modifier = Modifier
                                                                .padding(end = 5.dp)
                                                                .size(12.dp),
                                                        )
                                                    }
                                                }

                                                Text(
                                                    timeUi.title,
                                                    modifier = Modifier
                                                        .padding(top = 1.dp),
                                                    fontSize = 12.sp,
                                                    lineHeight = 14.sp,
                                                    color = c.white,
                                                )
                                            }

                                            Text(
                                                timeUi.timeLeftText,
                                                modifier = Modifier
                                                    .padding(start = 6.dp),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.W300,
                                                color = timeUi.timeLeftColor.toColor(),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }

                                        is TasksTabTasksVm.TaskVmUi.TimeUi.RegularUi -> {
                                            Text(
                                                timeUi.text,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp,
                                                fontWeight = FontWeight.W300,
                                                color = timeUi.textColor.toColor(),
                                            )
                                        }
                                    }
                                }
                            }

                            HStack(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {

                                Text(
                                    text = taskVmUi.text,
                                    color = c.text,
                                    modifier = Modifier
                                        .weight(1f),
                                )

                                TriggersListIconsView(taskVmUi.taskUi.tf.triggers, 14.sp)

                                if (taskVmUi.taskUi.tf.isImportant) {
                                    Icon(
                                        painterResource(R.drawable.sf_flag_fill_medium_regular),
                                        contentDescription = "Important",
                                        tint = c.red,
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .offset(y = 1.dp)
                                            .size(16.dp),
                                    )
                                }
                            }
                        }

                        if (!isFirst)
                            DividerBg()
                    }
                }
            }
        }

        if (tmrwUi != null) {

            item {
                Divider(
                    Modifier
                        .padding(horizontal = 80.dp)
                        .padding(top = 22.dp, bottom = if (tasksVmUi.isEmpty()) 0.dp else 18.dp)
                )
            }

            val tmrwTasksUi = tmrwUi.tasksUi
            items(
                tmrwTasksUi,
                key = { taskUI -> "tmrw_${taskUI.taskDb.id}" }
            ) { taskUi ->

                val isFirst = taskUi == tmrwTasksUi.lastOrNull()

                TasksListView__TmrwTaskView(
                    taskUi = taskUi,
                    isFirst = isFirst,
                )
            }
        }
    }
}

@Composable
private fun TasksListView__TmrwTaskView(
    taskUi: TasksTabTasksVm.TmrwTaskUi,
    isFirst: Boolean,
) {
    Column(
        modifier = Modifier
            .padding(start = H_PADDING),
        verticalArrangement = Arrangement.Center,
    ) {

        if (!isFirst)
            DividerBg()

        Box(Modifier.height(8.dp))

        val vPadding = 3.dp

        val timeUi = taskUi.timeUi
        if (timeUi != null) {
            Text(
                text = timeUi.text,
                modifier = Modifier
                    .padding(bottom = vPadding),
                fontSize = 13.sp,
                fontWeight = FontWeight.W300,
                color = timeUi.textColor.toColor(),
            )
        }

        HStack(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                taskUi.text,
                color = c.text,
                modifier = Modifier
                    .weight(1f),
            )
            TriggersListIconsView(taskUi.textFeatures.triggers, 14.sp)
        }

        Box(Modifier.height(8.dp))
    }
}
