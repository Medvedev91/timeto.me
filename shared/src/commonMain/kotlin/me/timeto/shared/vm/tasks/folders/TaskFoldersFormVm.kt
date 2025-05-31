package me.timeto.shared.vm.tasks.folders

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.launchExIo
import me.timeto.shared.onEachExIn
import me.timeto.shared.DialogsManager
import me.timeto.shared.moveUiListAndroid
import me.timeto.shared.moveUiListIos
import me.timeto.shared.vm.__Vm

class TaskFoldersFormVm : __Vm<TaskFoldersFormVm.State>() {

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

    //
    // Move

    fun moveIos(fromIdx: Int, toIdx: Int) {
        state.value.foldersDb.moveUiListIos(fromIdx, toIdx) {
            TaskFolderDb.updateSortMany(it.reversed())
        }
    }

    fun moveAndroidLocal(fromIdx: Int, toIdx: Int) {
        state.value.foldersDb.moveUiListAndroid(fromIdx, toIdx) { newItems ->
            state.update { it.copy(foldersDb = newItems) }
        }
    }

    fun moveAndroidSync() {
        launchExIo {
            TaskFolderDb.updateSortMany(state.value.foldersDb.reversed())
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
