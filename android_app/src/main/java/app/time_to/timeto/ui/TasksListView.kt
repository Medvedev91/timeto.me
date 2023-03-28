package app.time_to.timeto.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import app.time_to.timeto.*
import app.time_to.timeto.R
import kotlinx.coroutines.delay
import timeto.shared.db.TaskFolderModel
import timeto.shared.launchEx
import timeto.shared.ui.TimeUI
import timeto.shared.vm.TasksListVM

private val TASKS_LIST_ITEM_MIN_HEIGHT = 42.dp

@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
fun TasksListView(
    activeFolder: TaskFolderModel,
    dragItem: MutableState<DragItem?>,
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
                .padding(start = TAB_TASKS_PADDING_START, end = TAB_TASKS_PADDING_END),
            reverseLayout = true,
            contentPadding = PaddingValues(top = 12.dp),
            state = listState,
        ) {

            item {

                var isFocused by remember { mutableStateOf(false) }
                val focusManager = LocalFocusManager.current
                val focusRequester = remember { FocusRequester() }

                val isLight = MaterialTheme.colors.isLight

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
                            .border(
                                width = 0.5.dp,
                                // Border has almost the same color for the light theme
                                color = if (isLight) c.background else c.white.copy(alpha = 0.4f),
                                shape = MySquircleShape()
                            )
                            .clip(MySquircleShape())
                            .background(if (isLight) c.background2.copy(alpha = 0.9f) else c.background.copy(alpha = 0.85f))
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
                                        .defaultMinSize(minHeight = TASKS_LIST_ITEM_MIN_HEIGHT)
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
                                .padding(top = 5.dp, bottom = 5.dp, end = 5.dp)
                                .fillMaxHeight()
                                .clip(MySquircleShape(45f))
                                .background(c.blue)
                                .clickable {
                                    if (vm.isAddFormInputEmpty()) {
                                        if (isFocused) focusManager.clearFocus()
                                        else focusRequester.requestFocus()
                                        return@clickable
                                    }

                                    vm.addTask {
                                        scope.launchEx {
                                            listState.animateScrollBy(
                                                -dpToPx(TASKS_LIST_ITEM_MIN_HEIGHT.value).toFloat(),
                                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                            )
                                            // In case if it's higher than TASKS_LIST_ITEM_MIN_HEIGHT
                                            listState.animateScrollToItem(0)
                                        }
                                        scope.launchEx {                                             // WTF Without delay() does not clear before close.
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
                                fontWeight = FontWeight.W600
                            )
                        }
                    }

                    if (isFocused && WindowInsets.isImeVisible)
                        Box(
                            modifier = Modifier
                                .consumedWindowInsets(PaddingValues(bottom = LocalTabsHeight.current))
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
                val startPadding = 18.dp

                val isLast = taskUI == tasksUI.lastOrNull()
                val isFirst = taskUI == tasksUI.firstOrNull()
                val clip = when {
                    isFirst && isLast -> MySquircleShape()
                    isFirst -> MySquircleShape(angles = listOf(false, false, true, true))
                    isLast -> MySquircleShape(angles = listOf(true, true, false, false))
                    else -> RoundedCornerShape(0.dp)
                }

                Box(
                    modifier = Modifier
                        .clip(clip)
                        .background(c.background2)
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
                                .background(c.background2)
                                .clickable {
                                    taskUI.start(
                                        onStarted = {
                                            // Without scope: "Method setCurrentState must be called on the main thread"
                                            scope.launchEx {
                                                gotoTimer()
                                            }
                                        },
                                        needSheet = {
                                            Sheet.show { layer ->
                                                TaskSheet(layer, taskUI.task)
                                            }
                                        },
                                    )
                                },
                            contentAlignment = Alignment.BottomCenter
                        ) {

                            Column(
                                modifier = Modifier
                                    .padding(vertical = 8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {

                                val vPadding = 6.dp

                                val daytimeUI = taskUI.textFeatures.timeUI
                                if (daytimeUI != null) {
                                    Row(
                                        modifier = Modifier
                                            .padding(
                                                start = startPadding,
                                                top = 2.dp,
                                                bottom = vPadding,
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {

                                        when (daytimeUI.type) {

                                            TimeUI.TYPE.EVENT -> {
                                                Row(
                                                    modifier = Modifier
                                                        .offset(x = (-1).dp)
                                                        .clip(MySquircleShape(len = 40f))
                                                        .background(daytimeUI.color.toColor())
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
                                                        daytimeUI.daytimeText,
                                                        fontSize = 12.sp,
                                                        color = c.white,
                                                    )
                                                }
                                                Text(
                                                    daytimeUI.timeLeftText,
                                                    modifier = Modifier
                                                        .padding(start = 6.dp),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.W300,
                                                    color = daytimeUI.color.toColor(),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }

                                            TimeUI.TYPE.REPEATING -> {
                                                Text(
                                                    daytimeUI.daytimeText + "  " + daytimeUI.timeLeftText,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.W300,
                                                    color = daytimeUI.color.toColor(),
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
                                        .padding(horizontal = startPadding),
                                )

                                TextFeaturesTriggersView(
                                    triggers = taskUI.textFeatures.triggers,
                                    modifier = Modifier.padding(top = vPadding),
                                    contentPadding = PaddingValues(horizontal = startPadding - 2.dp),
                                )
                            }

                            if (!isFirst)
                                Divider(
                                    color = c.dividerBackground2,
                                    modifier = Modifier
                                        .padding(start = startPadding),
                                    thickness = 0.5.dp
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

            val daytimeUI = taskUI.textFeatures.timeUI
            if (daytimeUI != null) {
                Text(
                    daytimeUI.daytimeText,
                    modifier = Modifier
                        .padding(
                            start = startPadding,
                            top = 2.dp,
                            bottom = vPadding,
                        ),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W300,
                    color = daytimeUI.color.toColor(),
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
