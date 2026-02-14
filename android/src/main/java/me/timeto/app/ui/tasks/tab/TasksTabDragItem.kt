package me.timeto.app.ui.tasks.tab

import androidx.compose.runtime.MutableState

class TasksTabDragItem(
    val focusedDrop: MutableState<TasksTabDropItem?>,
    val isDropAllowed: (drop: TasksTabDropItem) -> Boolean,
    val onDrop: (drop: TasksTabDropItem) -> Unit,
)
