package me.timeto.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.TaskFolderModel
import me.timeto.shared.db.TaskFolderModel.Companion.sortedFolders
import me.timeto.shared.db.TaskModel
import me.timeto.shared.TextFeatures.TimeData
import me.timeto.shared.vm.__VM
import me.timeto.shared.vm.ui.sortedByFolder

class WatchTabTasksVM : __VM<WatchTabTasksVM.State>() {

    class TaskUI(
        val task: TaskModel,
    ) {

        val textFeatures = task.text.textFeatures()
        val listText = textFeatures.textUi()

        val timeUI: TimeUI? = textFeatures.timeData?.let { timeData ->
            val unixTime = timeData.unixTime
            val timeLeftText = timeData.timeLeftText()
            val daytimeText = daytimeToString(unixTime.time - unixTime.localDayStartTime())
            val textColor = when (timeData.status) {
                TimeData.STATUS.IN -> ColorNative.textSecondary
                TimeData.STATUS.NEAR -> ColorNative.blue
                TimeData.STATUS.OVERDUE -> ColorNative.red
            }
            TimeUI(
                text = "$daytimeText  $timeLeftText",
                textColor = textColor,
            )
        }

        fun start(
            onStarted: () -> Unit,
            needSheet: () -> Unit, // todo data for sheet
        ) {
            val autostartData = taskAutostartData(task) ?: return needSheet()
            launchExDefault {
                WatchToIosSync.startTaskWithLocal(
                    activity = autostartData.first,
                    deadline = autostartData.second,
                    task = task,
                )
                onStarted()
            }
        }

        class TimeUI(
            val text: String,
            val textColor: ColorNative,
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

    override fun onAppear() {
        val scope = scopeVM()
        TaskModel.getAscFlow().onEachExIn(scope) { tasks ->
            upFolders(tasks)
        }
        TaskFolderModel.anyChangeFlow().onEachExIn(scope) {
            upFolders(TaskModel.getAsc())
        }
    }

    private suspend fun upFolders(allTasks: List<TaskModel>) {
        val foldersUI = TaskFolderModel.getAscBySort().sortedFolders().map { folder ->
            FolderUI(
                title = folder.name,
                tasks = allTasks
                    .filter { it.folder_id == folder.id }
                    .sortedByFolder(folder)
                    .map { TaskUI(it) }
            )
        }
        state.update { it.copy(foldersUI = foldersUI) }
    }
}
