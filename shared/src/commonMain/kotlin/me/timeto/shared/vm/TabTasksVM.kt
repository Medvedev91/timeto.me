package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.timeto.shared.*
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.db.TaskFolderDb.Companion.sortedFolders
import me.timeto.shared.delayToNextMinute

class TabTasksVM : __VM<TabTasksVM.State>() {

    data class TaskFolderUI(
        val folder: TaskFolderDb,
    ) {
        val tabText = folder.name.toTabText()
    }

    data class State(
        val taskFoldersUI: List<TaskFolderUI>,
        val tabCalendarText: String,
        val initFolder: TaskFolderDb = DI.getTodayFolder(),
    )

    override val state = MutableStateFlow(
        State(
            taskFoldersUI = DI.taskFolders.sortedFolders().map { TaskFolderUI(it) },
            tabCalendarText = getTabCalendarText(),
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        TaskFolderDb.getAscBySortFlow().onEachExIn(scope) { folders ->
            val taskFoldersUI = folders.sortedFolders().map { TaskFolderUI(it) }
            state.update { it.copy(taskFoldersUI = taskFoldersUI) }
        }
        scope.launch {
            while (true) {
                delayToNextMinute()
                val newTabCalendarText = getTabCalendarText()
                if (newTabCalendarText != state.value.tabCalendarText)
                    state.update { it.copy(tabCalendarText = newTabCalendarText) }
            }
        }
    }
}

private fun String.toTabText(): String =
    this.uppercase().split("").joinToString("\n").trim()

private fun getTabCalendarText(): String {
    val unixTime = UnixTime()
    val dayStr = unixTime
        .getStringByComponents(UnixTime.StringComponent.dayOfMonth)
        .padStart(2, '0')
    val monthStr = unixTime
        .month()
        .toString()
        .padStart(2, '0')
    return "$dayStr\n$monthStr"
}
