package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.DI
import me.timeto.shared.UnixTime
import me.timeto.shared.db.TaskFolderModel
import me.timeto.shared.db.TaskFolderModel.Companion.sortedFolders
import me.timeto.shared.onEachExIn

class TabTasksVM : __VM<TabTasksVM.State>() {

    data class TaskFolderUI(
        val folder: TaskFolderModel,
    ) {
        val tabText = folder.name.toTabText()
    }

    data class State(
        val taskFoldersUI: List<TaskFolderUI>,
        val tabCalendarText: String,
    )

    override val state = MutableStateFlow(
        State(
            taskFoldersUI = DI.taskFolders.sortedFolders().map { TaskFolderUI(it) },
            tabCalendarText = getTabCalendarText(),
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        TaskFolderModel.getAscBySortFlow().onEachExIn(scope) { folders ->
            val taskFoldersUI = folders.sortedFolders().map { TaskFolderUI(it) }
            state.update { it.copy(taskFoldersUI = taskFoldersUI) }
        }
    }
}

private fun String.toTabText(): String =
    this.uppercase().split("").joinToString("\n").trim()

private fun getTabCalendarText(): String =
    UnixTime()
        .getStringByComponents(
            UnixTime.StringComponent.dayOfMonth,
            UnixTime.StringComponent.dayOfWeek2,
        )
        .toTabText()
