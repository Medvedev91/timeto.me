package me.timeto.app.ui.tasks.tab.tasks

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
import me.timeto.app.R
import me.timeto.app.Haptic
import me.timeto.app.toColor
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.SquircleShape
import me.timeto.app.ui.SwipeToAction
import me.timeto.app.ui.SwipeToAction__DeleteView
import me.timeto.app.ui.SwipeToAction__StartView
import me.timeto.app.ui.TriggersIconsView
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.events.EventFormFs
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.onePx
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.squircleShape
import me.timeto.app.ui.tasks.TaskTimerFs
import me.timeto.app.ui.tasks.form.TaskFormFs
import me.timeto.app.ui.tasks.tab.TasksTabDragItem
import me.timeto.app.ui.tasks.tab.TasksTabDropItem
import me.timeto.app.ui.tasks.tab.TasksTabView__LIST_SECTION_PADDING
import me.timeto.app.ui.tasks.tab.TasksTabView__PADDING_END
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.launchEx
import me.timeto.shared.vm.tasks.form.TaskFormStrategy
import me.timeto.shared.vm.tasks.tab.tasks.TasksTabTasksVm

private val inputShape = SquircleShape(16.dp)
private val highlightTimeShape = SquircleShape(8.dp)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TasksTabTasksView(
    taskFolderDb: TaskFolderDb,
    dragItem: MutableState<TasksTabDragItem?>,
) {

    val navigationFs = LocalNavigationFs.current

    val (_, state) = rememberVm(taskFolderDb) {
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
                        .border(width = onePx, color = c.gray5, shape = inputShape)
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
                            text = "SAVE",
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
                        text = tmrwUi.curTimeString,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W300,
                        color = c.secondaryText,
                    )
                }
            }
        }

        val tasksVmUi = state.tasksVmUi
        items(
            items = tasksVmUi,
            key = { it.taskUi.taskDb.id }
        ) { taskVmUi ->
            val isFirst = taskVmUi == tasksVmUi.firstOrNull()

            ZStack(
                modifier = Modifier
                    .background(c.bg),
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
                                    Haptic.shot()
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
                                    Haptic.long()
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
                            Haptic.long()
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
                                val taskDb = taskVmUi.taskUi.taskDb
                                taskDb.startIntervalForUi(
                                    ifJustStarted = {},
                                    ifTimerNeeded = {
                                        navigationFs.push {
                                            TaskTimerFs(
                                                taskDb = taskDb,
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
                                                    .background(timeUi.backgroundColorEnum.toColor())
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
                                                    text = timeUi.title,
                                                    modifier = Modifier
                                                        .padding(top = 1.dp),
                                                    fontSize = 12.sp,
                                                    lineHeight = 14.sp,
                                                    color = c.white,
                                                )
                                            }

                                            Text(
                                                text = timeUi.timeLeftText,
                                                modifier = Modifier
                                                    .padding(start = 6.dp),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.W300,
                                                color = timeUi.timeLeftColorEnum.toColor(),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }

                                        is TasksTabTasksVm.TaskVmUi.TimeUi.RegularUi -> {
                                            Text(
                                                text = timeUi.text,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp,
                                                fontWeight = FontWeight.W300,
                                                color = timeUi.textColorEnum.toColor(),
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

                                TriggersIconsView(
                                    checklistsDb = taskVmUi.taskUi.tf.checklistsDb,
                                    shortcutsDb = taskVmUi.taskUi.tf.shortcutsDb,
                                )

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
                            Divider()
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
                items = tmrwTasksUi,
                key = { taskUI -> "tmrw_${taskUI.taskDb.id}" }
            ) { taskUi ->

                val isFirst = taskUi == tmrwTasksUi.lastOrNull()

                TmrwTaskView(
                    taskUi = taskUi,
                    isFirst = isFirst,
                )
            }
        }
    }
}

@Composable
private fun TmrwTaskView(
    taskUi: TasksTabTasksVm.TmrwTaskUi,
    isFirst: Boolean,
) {
    Column(
        modifier = Modifier
            .padding(start = H_PADDING),
        verticalArrangement = Arrangement.Center,
    ) {

        if (!isFirst)
            Divider()

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
                color = timeUi.textColorEnum.toColor(),
            )
        }

        HStack(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = taskUi.text,
                color = c.text,
                modifier = Modifier
                    .weight(1f),
            )
            TriggersIconsView(
                checklistsDb = taskUi.textFeatures.checklistsDb,
                shortcutsDb = taskUi.textFeatures.shortcutsDb,
            )
        }

        Box(Modifier.height(8.dp))
    }
}
