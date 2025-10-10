package me.timeto.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.TextFeatures.TimeData
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.vm.Vm

class WatchTabTasksVm : Vm<WatchTabTasksVm.State>() {

    class TaskUI(
        val task: TaskDb,
    ) {

        val textFeatures = task.text.textFeatures()
        val listText = textFeatures.textUi()

        val timeUI: TimeUI? = textFeatures.calcTimeData()?.let { timeData ->
            val unixTime = timeData.unixTime
            val timeLeftText = timeData.timeLeftText()
            val daytimeText: String =
                DaytimeUi.byDaytime(unixTime.time - unixTime.localDayStartTime()).text
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
                    goalDb = autostartData.first,
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

// todo works different with mobile
fun taskAutostartData(
    task: TaskDb,
): Pair<Goal2Db, Int>? {
    val textFeatures = task.text.textFeatures()
    val goalDb = textFeatures.goalDb ?: return null
    val timer = textFeatures.timer ?: return null
    return goalDb to timer
}
