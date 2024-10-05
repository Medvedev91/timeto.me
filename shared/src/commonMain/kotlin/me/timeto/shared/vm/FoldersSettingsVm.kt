package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.TaskFolderDb

class FoldersSettingsVm : __Vm<FoldersSettingsVm.State>() {

    class TmrwButtonUi {

        val text = "Add \"Tomorrow\" Folder"

        fun add(): Unit = launchExIo {
            if (TaskFolderDb.selectAllSorted().any { it.isTmrw }) {
                showUiAlert("Tmrw already exists", "Tmrw already exists")
                return@launchExIo
            }
            TaskFolderDb.insertTmrw()
        }
    }

    data class State(
        val folders: List<TaskFolderDb>,
    ) {
        val headerTitle = "Folders"
        val tmrwButtonUi: TmrwButtonUi? =
            if (folders.any { it.isTmrw }) null else TmrwButtonUi()
    }

    override val state = MutableStateFlow(
        State(
            folders = Cache.taskFolders.toUiList()
        )
    )

    override fun onAppear() {
        val scope = scopeVm()
        TaskFolderDb.selectAllSortedFlow().onEachExIn(scope) { folders ->
            state.update { it.copy(folders = folders.toUiList()) }
        }
    }

    fun sortUp(folder: TaskFolderDb) {
        val tmpFolders = state.value.folders.toMutableList()
        val curIndex = tmpFolders.indexOf(folder)
        if ((curIndex <= 0) || ((curIndex + 1) > tmpFolders.size))
            return // todo report

        val prepItemIndex = curIndex - 1
        val prevItem = tmpFolders[prepItemIndex]
        tmpFolders[prepItemIndex] = folder
        tmpFolders[curIndex] = prevItem

        launchExDefault {
            tmpFolders.reversed().forEachIndexed { newIndex, folder ->
                folder.upSort(newIndex)
            }
        }
    }
}

private fun List<TaskFolderDb>.toUiList(): List<TaskFolderDb> =
    reversed()
