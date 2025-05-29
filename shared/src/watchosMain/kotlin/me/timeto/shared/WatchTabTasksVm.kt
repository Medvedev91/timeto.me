package me.timeto.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.TextFeatures.TimeData
import me.timeto.shared.misc.ColorEnum
import me.timeto.shared.ui.tasks.TaskUi
import me.timeto.shared.ui.tasks.sortedUi
import me.timeto.shared.vm.__Vm

class WatchTabTasksVm : __Vm<WatchTabTasksVm.State>() {

    class TaskUI(
        val task: TaskDb,
    ) {

        val textFeatures = task.text.textFeatures()
        val listText = textFeatures.textUi()

        val timeUI: TimeUI? = textFeatures.calcTimeData()?.let { timeData ->
            val unixTime = timeData.unixTime
            val timeLeftText = timeData.timeLeftText()
            val daytimeText = daytimeToString(unixTime.time - unixTime.localDayStartTime())
            val textColorEnum: ColorEnum = when (timeData.status) {
                TimeData.STATUS.IN -> ColorEnum.secondaryText
                TimeData.STATUS.SOON -> ColorEnum.blue
                TimeData.STATUS.OVERDUE -> ColorEnum.red
            }
            TimeUI(
                text = "$daytimeText  $timeLeftText",
                textColorEnum = textColorEnum,
            )
        }

        fun start(
            onStarted: () -> Unit,
            needSheet: () -> Unit, // todo data for sheet
        ) {
            val autostartData = taskAutostartData(task) ?: return needSheet()
            launchExIo {
                WatchToIosSync.startTaskWithLocal(
                    activity = autostartData.first,
                    timer = autostartData.second,
                    task = task,
                )
                onStarted()
            }
        }

        class TimeUI(
            val text: String,
            val textColorEnum: ColorEnum,
        )
    }

    data class FolderUI(
        val title: String,
        val tasks: List<TaskUI>,
    )

    data class State(
        val foldersUI: List<FolderUI>
    )

    override val state = MutableStateFlow(
        State(
            foldersUI = listOf()
        )
    )

    init {
        val scope = scopeVm()
        TaskDb.selectAscFlow().onEachExIn(scope) { tasks ->
            upFolders(tasks)
        }
        TaskFolderDb.anyChangeFlow().onEachExIn(scope) {
            upFolders(TaskDb.selectAsc())
        }
    }

    private suspend fun upFolders(allTasks: List<TaskDb>) {
        val foldersUI = TaskFolderDb.selectAllSorted().map { folder ->
            FolderUI(
                title = folder.name,
                tasks = allTasks
                    .filter { it.folder_id == folder.id }
                    .map { TaskUi(it) }
                    .sortedUi(isToday = folder.isToday)
                    .map { TaskUI(it.taskDb) }
            )
        }
        state.update { it.copy(foldersUI = foldersUI) }
    }
}
