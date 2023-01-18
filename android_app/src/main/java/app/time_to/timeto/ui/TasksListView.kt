package app.time_to.timeto.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.*
import app.time_to.timeto.*
import kotlinx.coroutines.delay
import timeto.shared.db.TaskFolderModel
import timeto.shared.db.TaskModel
import timeto.shared.launchEx
import timeto.shared.vm.TasksListVM

private val TASKS_LIST_ITEM_MIN_HEIGHT = 42.dp

@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
fun TasksListView(
    activeFolder: TaskFolderModel,
    dragItem: MutableState<DragItem?>,
) {
    val (_, state) = rememberVM(activeFolder) { TasksListVM(activeFolder) }

    val scope = rememberCoroutineScope()

    val editedTask = remember { mutableStateOf<TaskModel?>(null) }

    // Without the default value - animation on open
    val taskFormHeight = remember { mutableStateOf(TASKS_LIST_ITEM_MIN_HEIGHT + taskListSectionPadding * 2) }

    Box(
        modifier = Modifier
            .consumedWindowInsets(PaddingValues(bottom = LocalTabsHeight.current))
            .imePadding(),
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
                Box(modifier = Modifier.height(taskFormHeight.value))
            }

            val tasksUI = state.tasksUI
            items(
                tasksUI,
                key = { taskUI -> taskUI.task.id }
            ) { taskUI ->
                val isAddCalendarPresented = remember { mutableStateOf(false) }
                EventFormSheet(
                    isPresented = isAddCalendarPresented,
                    editedEvent = null,
                    defText = taskUI.task.text,
                ) {
                    taskUI.delete()
                }

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

                    val isSheetPresented = remember { mutableStateOf(false) }
                    TaskSheet(isSheetPresented, taskUI.task)

                    val ignoreOneSwipeToAction = remember { mutableStateOf(false) }
                    val isEditOrDelete = remember { mutableStateOf<Boolean?>(null) }
                    val stateOffsetAbsDp = remember { mutableStateOf(0.dp) }

                    val localDragItem = remember {
                        DragItem(
                            mutableStateOf(null),
                            when {
                                activeFolder.isInbox -> listOf(
                                    DropItem.TYPE.CALENDAR,
                                    DropItem.TYPE.WEEK,
                                    DropItem.TYPE.TODAY,
                                )
                                activeFolder.isWeek -> listOf(
                                    DropItem.TYPE.CALENDAR,
                                    DropItem.TYPE.INBOX,
                                    DropItem.TYPE.TODAY,
                                )
                                activeFolder.isToday -> listOf(
                                    DropItem.TYPE.CALENDAR,
                                    DropItem.TYPE.INBOX,
                                    DropItem.TYPE.WEEK,
                                )
                                else -> throw Exception()
                            }
                        ) { target ->
                            // Otherwise, the mod of editing is activated, so the keyboard is started.
                            // And since the task is transferred, sometimes just opens the keyboard.
                            ignoreOneSwipeToAction.value = true
                            scope.launchEx {
                                val whenRes = when (target.type) {
                                    DropItem.TYPE.INBOX -> {
                                        vibrateLong()
                                        taskUI.upFolder(TaskFolderModel.getInbox())
                                    }
                                    DropItem.TYPE.WEEK -> {
                                        vibrateLong()
                                        taskUI.upFolder(TaskFolderModel.getWeek())
                                    }
                                    DropItem.TYPE.TODAY -> {
                                        vibrateLong()
                                        taskUI.upFolder(TaskFolderModel.getToday())
                                    }
                                    DropItem.TYPE.CALENDAR -> {
                                        vibrateShort()
                                        isAddCalendarPresented.value = true
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
                            editedTask.value = taskUI.task
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
                                            isSheetPresented.value = true
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

                                val badgesHPadding = startPadding - 2.dp
                                val badgesVPadding = 6.dp

                                val daytimeUI = taskUI.textFeatures.timeUI
                                if (daytimeUI != null) {
                                    Text(
                                        daytimeUI.daytimeText + "  " + daytimeUI.timeLeftText,
                                        modifier = Modifier
                                            .padding(
                                                start = badgesHPadding,
                                                top = 2.dp,
                                                bottom = badgesVPadding,
                                            )
                                            .padding(start = 2.dp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.W300,
                                        color = daytimeUI.color.toColor(),
                                    )
                                }

                                Text(
                                    taskUI.listText,
                                    color = c.text,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = startPadding),
                                )

                                TriggersView__ListView(
                                    triggers = taskUI.textFeatures.triggers,
                                    withOnClick = true,
                                    modifier = Modifier.padding(top = badgesVPadding),
                                    contentPadding = PaddingValues(horizontal = badgesHPadding),
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
        }

        TaskFormView(
            height = taskFormHeight,
            folder = activeFolder,
            listState = listState,
            editedTask = editedTask,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun TaskFormView(
    height: MutableState<Dp>,
    folder: TaskFolderModel,
    listState: LazyListState,
    editedTask: MutableState<TaskModel?>,
) {
    val scope = rememberCoroutineScope()
    val errorDialog = LocalErrorDialog.current

    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val triggersState = TriggersView__State__TextField.asState(initText = "")

    LaunchedEffect(editedTask.value) {
        triggersState.reInit(editedTask.value?.text ?: "")
        if (editedTask.value != null) {
            focusRequester.requestFocus()
            // In case if field already focused but ime is not opened
            keyboardController?.show()
        }
    }

    // Input UI
    val isLight = MaterialTheme.colors.isLight
    val inputBg = if (isLight) c.background2.copy(alpha = 0.9f) else c.background.copy(alpha = 0.85f)
    // Border has almost the same color for the light theme
    val inputBorder = if (isLight) c.background else c.white.copy(alpha = 0.4f)
    val paddingStart = TAB_TASKS_PADDING_START - if (isLight) 1.dp else 0.dp
    val paddingEnd = TAB_TASKS_PADDING_END - if (isLight) 1.dp else 0.dp

    Column(
        modifier = Modifier
            .pointerInput(Unit) { } // Ignore clicks through
            .onSizeChanged {
                height.value = pxToDp(it.height).dp
            }
            .padding(
                top = taskListSectionPadding,
                bottom = taskListSectionPadding,
                start = paddingStart,
                end = paddingEnd
            )
    ) {

        /**
         * todo if (isFocused && WindowInsets.isImeVisible)
         * The "if" UI works faster than AnimatedVisibility, but I
         * get a crash when adding a task. I do not know the reason,
         * but if I don't use isFocused it doesn't crash.
         */

        val animationSpecIntSize: FiniteAnimationSpec<IntSize> = spring(
            stiffness = Spring.StiffnessHigh,
            visibilityThreshold = IntSize.VisibilityThreshold
        )
        val animationSpecFloat: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessHigh)

        AnimatedVisibility(
            visible = isFocused && WindowInsets.isImeVisible,
            modifier = Modifier
                .fillMaxWidth(), // Fix slide from start animation
            enter = expandVertically(animationSpecIntSize) + fadeIn(animationSpecFloat),
            exit = shrinkVertically(animationSpecIntSize) + fadeOut(animationSpecFloat),
        ) {
            TriggersView__FormView__Deprecated(
                triggersState = triggersState,
                modifier = Modifier
                    .padding(bottom = 12.dp)
            )
        }

        /****/

        Row(
            modifier = Modifier
                .border(0.5.dp, inputBorder, shape = MySquircleShape())
                .clip(MySquircleShape())
                .background(inputBg)
                .height(IntrinsicSize.Min), // To use fillMaxHeight() inside
            verticalAlignment = Alignment.CenterVertically
        ) {

            BasicTextField(
                value = triggersState.textField.value,
                onValueChange = {
                    triggersState.textField.value = it
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
                        if (triggersState.textField.value.text.isEmpty())
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

            if (editedTask.value != null) {
                Text(
                    text = "Cancel",
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .clip(RoundedCornerShape(99f))
                        .clickable {
                            editedTask.value = null
                            focusManager.clearFocus()
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    color = c.textSecondary,
                    fontSize = 14.sp,
                )
            }

            Box(
                modifier = Modifier
                    .padding(top = 5.dp, bottom = 5.dp, end = 5.dp)
                    .fillMaxHeight()
                    .clip(MySquircleShape(45f))
                    .background(c.blue)
                    .clickable {
                        if (triggersState.textField.value.text.isBlank()) {
                            if (isFocused) focusManager.clearFocus()
                            else focusRequester.requestFocus()
                            return@clickable
                        }


                        scope.launchEx {
                            try {
                                val newText = triggersState.textWithTriggers()

                                val editedTaskValue = editedTask.value
                                if (editedTaskValue != null) {
                                    editedTaskValue.upTextWithValidation(newText)
                                    editedTask.value = null
                                } else {
                                    TaskModel.addWithValidation(newText, folder)

                                    scope.launchEx {
                                        listState.animateScrollBy(
                                            -dpToPx(TASKS_LIST_ITEM_MIN_HEIGHT.value).toFloat(),
                                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                        )
                                        // In case if it's higher than TASKS_LIST_ITEM_MIN_HEIGHT
                                        listState.animateScrollToItem(0)
                                    }
                                }

                                triggersState.textField.value = TextFieldValue()
                                triggersState.triggers.clear()

                                // WTF Without delay() does not clear before close.
                                // clearFocus() to change isFocused as fast as possible.
                                // During delay() adding animation.
                                delay(250)
                                focusManager.clearFocus()
                                ////

                            } catch (e: MyException) {
                                errorDialog.value = e.uiMessage
                            }
                        }
                    }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    if (editedTask.value == null) "SAVE" else "UP",
                    color = c.white,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W600
                )
            }
        }
    }
}
