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

    fun moveIos(fromIdx: Int, toIdx: Int) {
        state.value.foldersDb.moveIos(fromIdx, toIdx) {
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
