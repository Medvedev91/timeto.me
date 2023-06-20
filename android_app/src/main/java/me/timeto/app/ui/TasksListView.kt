package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import me.timeto.app.*
import me.timeto.app.R
import kotlinx.coroutines.delay
import me.timeto.shared.db.TaskFolderModel
import me.timeto.shared.launchEx
import me.timeto.shared.vm.TasksListVM

@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
fun TasksListView(
    activeFolder: TaskFolderModel,
    dragItem: MutableState<DragItem?>,
    onTaskStarted: () -> Unit,
) {
    val (vm, state) = rememberVM(activeFolder) { TasksListVM(activeFolder) }
    val tmrwData = state.tmrwData

    val scope = rememberCoroutineScope()

    Box(
        contentAlignment = Alignment.BottomCenter,
    ) {

        val listState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = TAB_TASKS_PADDING_HALF_H, end = TAB_TASKS_PADDING_END),
            reverseLayout = true,
            contentPadding = PaddingValues(top = taskListSectionPadding),
            state = listState,
        ) {

            item {

                var isFocused by remember { mutableStateOf(false) }
                val focusManager = LocalFocusManager.current
                val focusRequester = remember { FocusRequester() }

                Column(
                    modifier = Modifier
                        .pointerInput(Unit) { } // Ignore clicks through
                        .padding(
                            top = taskListSectionPadding,
                            bottom = taskListSectionPadding,
                        )
                ) {

                    Row(
                        modifier = Modifier
                            .padding(horizontal = TAB_TASKS_PADDING_HALF_H - 4.dp)
                            .border(width = onePx, color = c.dividerBg, shape = roundedShape)
                            .height(IntrinsicSize.Min), // To use fillMaxHeight() inside
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        BasicTextField__VMState(
                            text = state.addFormInputTextValue,
                            onValueChange = {
                                vm.setAddFormInputTextValue(it)
                            },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            singleLine = false,
                            cursorBrush = SolidColor(MaterialTheme.colors.primary),
                            textStyle = LocalTextStyle.current.copy(
                                color = MaterialTheme.colors.onSurface,
                                fontSize = 16.sp
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 40.dp)
                                        .padding(start = 16.dp, end = 4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (state.addFormInputTextValue.isEmpty())
                                        Text(
                                            text = "Task",
                                            color = c.text.copy(alpha = 0.5f)
                                        )
                                    innerTextField()
                                }
                            },
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .weight(1f)
                                .onFocusChanged {
                                    isFocused = it.isFocused
                                }
                        )

                        Box(
                            modifier = Modifier
                                .padding(top = 5.dp, bottom = 5.dp, end = 5.dp)
                                .fillMaxHeight()
                                .clip(roundedShape)
                                .background(c.blue)
                                .clickable {
                                    if (vm.isAddFormInputEmpty()) {
                                        if (isFocused) focusManager.clearFocus()
                                        else focusRequester.requestFocus()
                                        return@clickable
                                    }

                                    vm.addTask {
                                        scope.launchEx {
                                            listState.animateScrollToItem(0)
                                        }
                                        scope.launchEx {
                                            // WTF Without delay() does not clear before close.
                                            // clearFocus() to change isFocused as fast as possible.
                                            // During delay() adding animation.
                                            delay(250)
                                            focusManager.clearFocus()
                                            ////
                                        }
                                    }
                                }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                "SAVE",
                                color = c.white,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.W600
                            )
                        }
                    }

                    if (isFocused && WindowInsets.isImeVisible)
                        Box(
                            modifier = Modifier
                                .consumeWindowInsets(PaddingValues(bottom = bottomNavigationHeight))
                                .imePadding()
                        )
                }
            }

            if (tmrwData != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = taskListSectionPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            tmrwData.curTimeString,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W300,
                            color = c.textSecondary,
                        )
                    }
                }
            }

            val tasksUI = state.tasksUI
            items(
                tasksUI,
                key = { taskUI -> taskUI.task.id }
            ) { taskUI ->
                val isFirst = taskUI == tasksUI.firstOrNull()

                Box(
                    modifier = Modifier
                        .clip(squircleShape)
                        .background(c.bg)
                    // .animateItemPlacement() // Slow rendering on IME open
                ) {

                    val ignoreOneSwipeToAction = remember { mutableStateOf(false) }
                    val isEditOrDelete = remember { mutableStateOf<Boolean?>(null) }
                    val stateOffsetAbsDp = remember { mutableStateOf(0.dp) }

                    val localDragItem = remember {
                        DragItem(
                            mutableStateOf(null),
                            { drop ->
                                when (drop) {
                                    is DropItem.Type__Calendar -> true
                                    is DropItem.Type__Folder -> drop.folder.id != taskUI.task.folder_id
                                }
                            }
                        ) { drop ->
                            // Otherwise, the mod of editing is activated, so the keyboard is started.
                            // And since the task is transferred, sometimes just opens the keyboard.
                            ignoreOneSwipeToAction.value = true
                            scope.launchEx {
                                when (drop) {
                                    is DropItem.Type__Calendar -> {
                                        vibrateShort()
                                        EventFormSheet__show(
                                            editedEvent = null,
                                            defText = taskUI.task.text,
                                        ) {
                                            taskUI.delete()
                                        }
                                    }
                                    is DropItem.Type__Folder -> {
                                        vibrateLong()
                                        taskUI.upFolder(drop.folder)
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
                                bgColor = if (dropItem != null) c.tasksTabDropFocused else c.blue
                            )
                        },
                        endView = { state ->
                            SwipeToAction__DeleteView(state, taskUI.task.text) {
                                vibrateLong()
                                taskUI.delete()
                            }
                        },
                        onStart = {
                            Sheet.show { layer ->
                                TaskFormSheet(
                                    task = taskUI.task,
                                    layer = layer,
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
                                    taskUI.task.startIntervalForUI(
                                        onStarted = {
                                            onTaskStarted()
                                        },
                                        needSheet = {
                                            Sheet.show { layer ->
                                                ActivitiesTimerSheet(layer, taskUI.timerContext, onTaskStarted)
                                            }
                                        },
                                    )
                                },
                            contentAlignment = Alignment.BottomCenter
                        ) {

                            Column(
                                modifier = Modifier
                                    .padding(top = 11.dp, bottom = 11.dp),
                                verticalArrangement = Arrangement.Center
                            ) {

                                val vPadding = 3.dp

                                val timeUI = taskUI.timeUI
                                if (timeUI != null) {
                                    Row(
                                        modifier = Modifier
                                            .padding(
                                                start = TAB_TASKS_PADDING_HALF_H,
                                                bottom = vPadding,
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {

                                        when (timeUI) {

                                            is TasksListVM.TaskUI.TimeUI.ImportantUI -> {
                                                Row(
                                                    modifier = Modifier
                                                        .offset(x = (-1).dp)
                                                        .clip(MySquircleShape(len = 30f))
                                                        .background(timeUI.backgroundColor.toColor())
                                                        .padding(start = 5.dp, end = 4.dp, top = 3.dp, bottom = 3.dp),
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
                                                        timeUI.title,
                                                        fontSize = 12.sp,
                                                        color = c.white,
                                                    )
                                                }
                                                Text(
                                                    timeUI.timeLeftText,
                                                    modifier = Modifier
                                                        .padding(start = 6.dp),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.W300,
                                                    color = timeUI.timeLeftColor.toColor(),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }

                                            is TasksListVM.TaskUI.TimeUI.RegularUI -> {
                                                Text(
                                                    timeUI.text,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.W300,
                                                    color = timeUI.textColor.toColor(),
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    taskUI.text,
                                    color = c.text,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = TAB_TASKS_PADDING_HALF_H),
                                )

                                TextFeaturesTriggersView(
                                    triggers = taskUI.textFeatures.triggers,
                                    modifier = Modifier.padding(top = vPadding + 2.dp),
                                    contentPadding = PaddingValues(horizontal = TAB_TASKS_PADDING_HALF_H - 2.dp),
                                )
                            }

                            if (!isFirst)
                                DividerBg(
                                    Modifier.padding(
                                        start = TAB_TASKS_PADDING_HALF_H,
                                        end = TAB_TASKS_PADDING_HALF_H,
                                    )
                                )
                        }
                    }
                }
            }

            if (tmrwData != null) {

                item {
                    Divider(
                        Modifier
                            .padding(horizontal = 80.dp)
                            .padding(top = 22.dp, bottom = if (tasksUI.isEmpty()) 0.dp else 18.dp)
                    )
                }

                val tmrwTasksUI = tmrwData.tasksUI
                items(
                    tmrwTasksUI,
                    key = { taskUI -> "tmrw_${taskUI.task.id}" }
                ) { taskUI ->

                    // Reversed
                    val isFirst = taskUI == tmrwTasksUI.lastOrNull()
                    val isLast = taskUI == tmrwTasksUI.firstOrNull()

                    TasksListView__TmrwTaskView(
                        taskUI = taskUI,
                        isFirst = isFirst,
                        isLast = isLast,
                    )
                }
            }
        }
    }
}

@Composable
private fun TasksListView__TmrwTaskView(
    taskUI: TasksListVM.TmrwTaskUI,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val startPadding = 18.dp

    MyListView__ItemView(
        isFirst = isFirst,
        isLast = isLast,
        withTopDivider = !isFirst,
        outerPadding = PaddingValues(0.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {

            val vPadding = 6.dp

            val timeUI = taskUI.timeUI
            if (timeUI != null) {
                Text(
                    text = timeUI.text,
                    modifier = Modifier
                        .padding(
                            start = startPadding,
                            top = 2.dp,
                            bottom = vPadding,
                        ),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W300,
                    color = timeUI.textColor.toColor(),
                )
            }

            Text(
                taskUI.text,
                color = c.text,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = startPadding),
            )

            TextFeaturesTriggersView(
                triggers = taskUI.textFeatures.triggers,
                modifier = Modifier.padding(top = vPadding),
                contentPadding = PaddingValues(horizontal = startPadding - 2.dp),
            )
        }
    }
}
