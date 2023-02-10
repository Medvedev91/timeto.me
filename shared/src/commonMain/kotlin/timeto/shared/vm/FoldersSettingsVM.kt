package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.*
import timeto.shared.db.TaskFolderModel
import timeto.shared.db.TaskFolderModel.Companion.sortedFolders

class FoldersSettingsVM : __VM<FoldersSettingsVM.State>() {

    class TmrwButtonUI {

        val text = "Add \"Tomorrow\" Folder"

        fun add(): Unit = launchExDefault {
            if (DI.getTmrwFolderOrNull() != null) {
                showUiAlert("Tmrw already exists", "Tmrw already exists")
                return@launchExDefault
            }
            TaskFolderModel.addTmrw()
        }
    }

    data class State(
        val folders: List<TaskFolderModel>,
    ) {
        val headerTitle = "Folders"
        val headerDoneText = "Done" // Strange but ok for now
        val tmrwButtonUI = if (folders.any { it.isTmrw }) null else TmrwButtonUI()
    }

    override val state = MutableStateFlow(
        State(
            folders = DI.taskFolders.toUIList()
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        TaskFolderModel.getAscBySortFlow().onEachExIn(scope) { folders ->
            state.update { it.copy(folders = folders.toUIList()) }
        }
    }

    fun sortUp(folder: TaskFolderModel) {
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

private fun List<TaskFolderModel>.toUIList() = this.sortedFolders().reversed()
