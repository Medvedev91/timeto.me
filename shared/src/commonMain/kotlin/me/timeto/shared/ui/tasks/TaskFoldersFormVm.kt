package me.timeto.shared.ui.tasks

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.launchExIo
import me.timeto.shared.onEachExIn
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.ui.moveIos
import me.timeto.shared.vm.__Vm

class TaskFoldersFormVm(
) : __Vm<TaskFoldersFormVm.State>() {

    data class State(
        val foldersDb: List<TaskFolderDb>,
    ) {

        val title = "Folders"

        val tmrwButtonUi: TmrwButtonUi? =
            if (foldersDb.any { it.isTmrw }) null else TmrwButtonUi()
    }

    override val state = MutableStateFlow(
        State(
            foldersDb = Cache.taskFoldersDbSorted.reversed(),
        )
    )

    init {
        val scopeVm = scopeVm()
        TaskFolderDb.selectAllSortedFlow().onEachExIn(scopeVm) { folders ->
            state.update { it.copy(foldersDb = folders.reversed()) }
        }
    }

    /*
    fun sortUp(folder: TaskFolderDb) {
        val tmpFolders = state.value.foldersDb.toMutableList()
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
    */

    fun moveIos(from: Int, to: Int) {
        state.value.foldersDb.moveIos(from, to) {
            TaskFolderDb.updateSortMany(it.reversed())
        }
    }

    ///

    class TmrwButtonUi {

        val text = "Add \"Tomorrow\" Folder"

        fun add(
            dialogsManager: DialogsManager,
        ): Unit = launchExIo {
            if (TaskFolderDb.selectAllSorted().any { it.isTmrw }) {
                dialogsManager.alert("Tmrw already exists")
                return@launchExIo
            }
            TaskFolderDb.insertTmrw()
        }
    }
}