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
import me.timeto.app.ui.main.MainTabsView__height
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.launchEx
import me.timeto.shared.vm.TasksListVm

private val inputShape = SquircleShape(16.dp)
private val highlightTimeShape = SquircleShape(8.dp)

@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
fun TasksListView(
    activeFolder: TaskFolderDb,
    dragItem: MutableState<DragItem?>,
) {
    val (vm, state) = rememberVm(activeFolder) { TasksListVm(activeFolder) }
    val tmrwData = state.tmrwData

    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight(),
        reverseLayout = true,
        contentPadding = PaddingValues(top = TasksView__LIST_SECTION_PADDING, end = TasksView__PADDING_END),
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
                        top = TasksView__LIST_SECTION_PADDING,
                        bottom = TasksView__LIST_SECTION_PADDING,
                    )
            ) {

                Row(
                    modifier = Modifier
                        .padding(start = H_PADDING - 2.dp)
                        .border(width = onePx, color = c.dividerBg, shape = inputShape)
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
                                    .defaultMinSize(minHeight = 42.dp)
                                    .padding(start = 14.dp, end = 4.dp),
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
                            .padding(top = 4.dp, bottom = 4.dp, end = 4.dp)
                            .fillMaxHeight()
                            .clip(squircleShape)
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
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W600,
                        )
                    }
                }

                if (isFocused && WindowInsets.isImeVisible)
                    Box(
                        modifier = Modifier
                            .consumeWindowInsets(PaddingValues(bottom = MainTabsView__height))
                            .imePadding()
                    )
            }
        }

        if (tmrwData != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = TasksView__LIST_SECTION_PADDING),
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

        val vmTasksUi = state.vmTasksUi
        items(
            vmTasksUi,
            key = { it.taskUi.taskDb.id }
        ) { vmTaskUi ->
            val isFirst = vmTaskUi == vmTasksUi.firstOrNull()

            Box(
                modifier = Modifier
                    .background(c.bg)
                // .animateItemPlacement() // Slow rendering on IME open
            ) {

                val ignoreOneSwipeToAction = remember { mutableStateOf(false) }
                val isEditOrDelete = remember { mutableStateOf<Boolean?>(null) }
                val stateOffsetAbsDp = remember { mutableStateOf(0.dp) }

                val localDragItem = remember(vmTasksUi) {
                    DragItem(
                        mutableStateOf(null),
                        { drop ->
                            when (drop) {
                                is DropItem.Type__Calendar -> true
                                is DropItem.Type__Folder -> drop.folder.id != vmTaskUi.taskUi.taskDb.folder_id
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
                                        defText = vmTaskUi.taskUi.taskDb.text,
                                    ) {
                                        vmTaskUi.delete()
                                    }
                                }
                                is DropItem.Type__Folder -> {
                                    vibrateLong()
                                    vmTaskUi.upFolder(drop.folder)
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
                        SwipeToAction__DeleteView(state, vmTaskUi.taskUi.taskDb.text) {
                            vibrateLong()
                            vmTaskUi.delete()
                        }
                    },
                    onStart = {
                        Sheet.show { layer ->
                            TaskFormSheet(
                                task = vmTaskUi.taskUi.taskDb,
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
                                vmTaskUi.taskUi.taskDb.startIntervalForUI(
                                    onStarted = {},
                                    activitiesSheet = {
                                        ActivitiesTimerSheet__show(vmTaskUi.timerContext, withMenu = false)
                                    },
                                    timerSheet = { activity ->
                                        ActivityTimerSheet__show(
                                            activity = activity,
                                            timerContext = vmTaskUi.timerContext,
                                        ) {}
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

                            val timeUI = vmTaskUi.timeUI
                            if (timeUI != null) {
                                Row(
                                    modifier = Modifier
                                        .padding(bottom = vPadding),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {

                                    when (timeUI) {

                                        is TasksListVm.VmTaskUi.TimeUI.HighlightUI -> {
                                            Row(
                                                modifier = Modifier
                                                    .offset(x = (-1).dp)
                                                    .clip(highlightTimeShape)
                                                    .background(timeUI.backgroundColor.toColor())
                                                    .padding(start = 5.dp, end = 4.dp, top = 3.dp, bottom = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {

                                                when (timeUI._timeData.type) {

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
                                                    timeUI.title,
                                                    modifier = Modifier
                                                        .padding(top = 1.dp),
                                                    fontSize = 12.sp,
                                                    lineHeight = 14.sp,
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

                                        is TasksListVm.VmTaskUi.TimeUI.RegularUI -> {
                                            Text(
                                                timeUI.text,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp,
                                                fontWeight = FontWeight.W300,
                                                color = timeUI.textColor.toColor(),
                                            )
                                        }
                                    }
                                }
                            }

                            HStack(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {

                                Text(
                                    vmTaskUi.text,
                                    color = c.text,
                                    modifier = Modifier
                                        .weight(1f),
                                )

                                TriggersListIconsView(vmTaskUi.taskUi.tf.triggers, 14.sp)

                                if (vmTaskUi.taskUi.tf.isImportant) {
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

        if (tmrwData != null) {

            item {
                Divider(
                    Modifier
                        .padding(horizontal = 80.dp)
                        .padding(top = 22.dp, bottom = if (vmTasksUi.isEmpty()) 0.dp else 18.dp)
                )
            }

            val tmrwTasksUI = tmrwData.tasksUI
            items(
                tmrwTasksUI,
                key = { taskUI -> "tmrw_${taskUI.task.id}" }
            ) { taskUI ->

                val isFirst = taskUI == tmrwTasksUI.lastOrNull()

                TasksListView__TmrwTaskView(
                    taskUI = taskUI,
                    isFirst = isFirst,
                )
            }
        }
    }
}

@Composable
private fun TasksListView__TmrwTaskView(
    taskUI: TasksListVm.TmrwTaskUI,
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

        val timeUI = taskUI.timeUI
        if (timeUI != null) {
            Text(
                text = timeUI.text,
                modifier = Modifier
                    .padding(bottom = vPadding),
                fontSize = 13.sp,
                fontWeight = FontWeight.W300,
                color = timeUI.textColor.toColor(),
            )
        }

        HStack(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                taskUI.text,
                color = c.text,
                modifier = Modifier
                    .weight(1f),
            )
            TriggersListIconsView(taskUI.textFeatures.triggers, 14.sp)
        }

        Box(Modifier.height(8.dp))
    }
}
