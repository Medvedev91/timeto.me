package me.timeto.app.ui.tasks.tab

import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import kotlinx.coroutines.delay
import me.timeto.app.ui.HStack
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.SquircleShape
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.tasks.tab.tasks.TasksTabTasksView
import me.timeto.app.ui.calendar.CalendarTabsView
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.tasks.tab.repeatings.TasksTabRepeatingsView
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.ui.tasks.tab.TasksTabVm
import kotlin.random.Random

val TasksTabView__TAB_BUTTON_WIDTH: Dp = 32.dp
val TasksTabView__PADDING_END: Dp = TasksTabView__TAB_BUTTON_WIDTH + H_PADDING
val TasksTabView__LIST_SECTION_PADDING: Dp = 20.dp

//

private val tabShape = SquircleShape(12.dp)
private val tabVPadding = 8.dp
private val tabActiveTextColor = c.white
private val tabInactiveTextColor = c.homeFontSecondary

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TasksTabView(
    onClose: () -> Unit,
) {

    val (_, state) = rememberVm {
        TasksTabVm()
    }

    var activeTab by remember {
        mutableStateOf<Tab>(Tab.Folder(state.initFolder))
    }

    BackHandler {
        if ((activeTab as? Tab.Folder)?.taskFolderDb?.isToday != true)
            activeTab = Tab.Folder(state.initFolder)
        else
            onClose()
    }

    val dragItem = remember { mutableStateOf<TasksTabDragItem?>(null) }
    val dropItems = remember { mutableListOf<TasksTabDropItem>() }
    fun setFocusedDrop(drop: TasksTabDropItem?) {
        dragItem.value?.focusedDrop?.value = drop
    }

    ZStack(
        modifier = Modifier
            .motionEventSpy { event ->
                val dragItemValue = dragItem.value ?: return@motionEventSpy

                val x = event.x
                val y = event.y
                val focusedDrop = dropItems
                    .filter { dragItemValue.isDropAllowed(it) }
                    .firstOrNull { drop ->
                        val s = drop.square
                        x > s.x1 && y > s.y1 && x < s.x2 && y < s.y2
                    }

                if (focusedDrop == null) {
                    setFocusedDrop(null)
                    return@motionEventSpy
                }

                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        dragItemValue.onDrop(focusedDrop)
                        setFocusedDrop(null)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (dragItemValue.focusedDrop.value == null)
                            vibrateShort()
                        setFocusedDrop(focusedDrop)
                    }
                    else -> {
                        setFocusedDrop(null)
                    }
                }
            }
            .background(c.bg),
        contentAlignment = Alignment.CenterEnd,
    ) {

        when (val curTab = activeTab) {
            is Tab.Folder -> TasksTabTasksView(curTab.taskFolderDb, dragItem)
            is Tab.Calendar -> CalendarTabsView()
            is Tab.Repeating -> TasksTabRepeatingsView()
        }

        Column(
            modifier = Modifier
                .width(TasksTabView__TAB_BUTTON_WIDTH)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {

            LazyColumn(
                reverseLayout = true,
            ) {

                item {
                    val dropItem = remember {
                        TasksTabDropItem.Calendar(TasksTabDropItem.Square(0, 0, 0, 0))
                    }
                    val isActive = activeTab is Tab.Calendar

                    CalendarButtonView(
                        isActive = isActive,
                        dragItem = dragItem,
                        dropItem = dropItem,
                        dropItems = dropItems,
                        onClick = {
                            if (isActive) onClose()
                            else activeTab = Tab.Calendar
                        },
                    )
                }

                items(state.taskFoldersUi) { folderUi ->
                    val dropItem = remember {
                        TasksTabDropItem.Folder(folderUi.taskFolderDb, TasksTabDropItem.Square(0, 0, 0, 0))
                    }
                    val isActive: Boolean =
                        (activeTab as? Tab.Folder)?.taskFolderDb?.id == folderUi.taskFolderDb.id
                    TabTextButton(
                        text = folderUi.tabText,
                        isActive = isActive,
                        dragItem = dragItem,
                        dropItem = dropItem,
                        dropItems = dropItems,
                        onClick = {
                            if (isActive) onClose()
                            else activeTab = Tab.Folder(folderUi.taskFolderDb)
                        },
                    )
                }

                //
                // Repeatings

                item {
                    val isActive = activeTab is Tab.Repeating
                    val backgroundColor = animateColorAsState(if (isActive) c.blue else c.bg, spring(stiffness = Spring.StiffnessMedium))
                    val textColor = animateColorAsState(if (isActive) tabActiveTextColor else tabInactiveTextColor, spring(stiffness = Spring.StiffnessMedium))

                    Box(
                        modifier = Modifier
                            .width(TasksTabView__TAB_BUTTON_WIDTH)
                            .height(TasksTabView__TAB_BUTTON_WIDTH)
                            .clip(tabShape)
                            .background(backgroundColor.value)
                            .clickable {
                                if (isActive) onClose()
                                else activeTab = Tab.Repeating
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(id = R.drawable.sf_repeat_medium_semibold),
                            contentDescription = "Repeating",
                            tint = textColor.value,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
        }
    }
}

private sealed class Tab {
    class Folder(val taskFolderDb: TaskFolderDb) : Tab()
    data object Calendar : Tab()
    data object Repeating : Tab()
}

@Composable
private fun TabTextButton(
    text: String,
    isActive: Boolean,
    dragItem: State<TasksTabDragItem?>,
    dropItem: TasksTabDropItem,
    dropItems: MutableList<TasksTabDropItem>,
    onClick: () -> Unit,
) {
    DisposableEffect(Unit) {
        dropItems.add(dropItem)
        onDispose {
            dropItems.remove(dropItem)
        }
    }

    val isAllowedToDrop = dragItem.value?.isDropAllowed?.invoke(dropItem) ?: false
    val isFocusedToDrop = dragItem.value?.focusedDrop?.value == dropItem

    val bgColor = animateColorAsState(
        when {
            isFocusedToDrop -> c.tasksDropFocused
            isAllowedToDrop -> c.purple
            isActive -> c.blue
            else -> c.bg
        },
        spring(stiffness = Spring.StiffnessMedium)
    )

    val textColor = animateColorAsState(
        when {
            isFocusedToDrop -> c.white
            isAllowedToDrop -> c.white
            isActive -> tabActiveTextColor
            else -> tabInactiveTextColor
        },
        spring(stiffness = Spring.StiffnessMedium)
    )

    val rotationMaxAngle = 3f
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    val rotationAngleAnimate by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = Random.nextInt(80, 130), easing = LinearEasing),
        finishedListener = {
            if (isAllowedToDrop)
                rotationAngle = if (rotationAngle < 0) rotationMaxAngle else -rotationMaxAngle
        }
    )
    LaunchedEffect(isAllowedToDrop) {
        if (isAllowedToDrop)
            delay(Random.nextInt(0, 50).toLong())
        rotationAngle = if (isAllowedToDrop) (if (Random.nextBoolean()) rotationMaxAngle else -rotationMaxAngle) else 0f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = tabVPadding)
            .rotate(rotationAngleAnimate)
            .onGloballyPositioned { c ->
                dropItem.upSquareByCoordinates(c)
            }
            .clip(tabShape)
            .background(bgColor.value)
    ) {

        Text(
            text,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center,
            color = textColor.value,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
        )
    }
}

//
// Calendar Button

private val calendarDots: List<List<Boolean>> = listOf(
    listOf(false, true, true, true),
    listOf(true, true, true, true),
    listOf(true, true, true, false),
)

@Composable
private fun CalendarButtonView(
    isActive: Boolean,
    dragItem: State<TasksTabDragItem?>,
    dropItem: TasksTabDropItem,
    dropItems: MutableList<TasksTabDropItem>,
    onClick: () -> Unit,
) {

    DisposableEffect(Unit) {
        dropItems.add(dropItem)
        onDispose {
            dropItems.remove(dropItem)
        }
    }

    val isAllowedToDrop = dragItem.value?.isDropAllowed?.invoke(dropItem) ?: false
    val isFocusedToDrop = dragItem.value?.focusedDrop?.value == dropItem

    val bgColor = animateColorAsState(
        when {
            isFocusedToDrop -> c.tasksDropFocused
            isAllowedToDrop -> c.purple
            isActive -> c.blue
            else -> c.bg
        },
        spring(stiffness = Spring.StiffnessMedium)
    )

    val textColor = animateColorAsState(
        when {
            isFocusedToDrop -> c.white
            isAllowedToDrop -> c.white
            isActive -> tabActiveTextColor
            else -> tabInactiveTextColor
        },
        spring(stiffness = Spring.StiffnessMedium)
    )

    val rotationMaxAngle = 3f
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    val rotationAngleAnimate by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = Random.nextInt(80, 130), easing = LinearEasing),
        finishedListener = {
            if (isAllowedToDrop)
                rotationAngle = if (rotationAngle < 0) rotationMaxAngle else -rotationMaxAngle
        }
    )
    LaunchedEffect(isAllowedToDrop) {
        if (isAllowedToDrop)
            delay(Random.nextInt(0, 50).toLong())
        rotationAngle = if (isAllowedToDrop) (if (Random.nextBoolean()) rotationMaxAngle else -rotationMaxAngle) else 0f
    }

    VStack(
        modifier = Modifier
            .padding(top = 11.dp)
            .rotate(rotationAngleAnimate)
            .onGloballyPositioned { c ->
                dropItem.upSquareByCoordinates(c)
            }
            .size(TasksTabView__TAB_BUTTON_WIDTH)
            .clip(tabShape)
            .clickable {
                onClick()
            }
            .background(bgColor.value)
            .padding(horizontal = 5.dp),
        verticalArrangement = Arrangement.Center,
    ) {

        calendarDots.forEachIndexed { idx, dots ->

            HStack(
                modifier = Modifier
                    .padding(top = if (idx == 0) 0.dp else 5.dp),
            ) {

                dots.forEach { dot ->

                    SpacerW1()

                    ZStack(
                        modifier = Modifier
                            .size(2.dp + onePx)
                            .clip(roundedShape)
                            .background(if (dot) textColor.value else c.transparent),
                    )
                }

                SpacerW1()
            }
        }
    }
}
