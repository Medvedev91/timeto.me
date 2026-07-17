package me.timeto.shared.vm.home.bar

import me.timeto.shared.NoteFolderUi
import me.timeto.shared.TaskFolderUi
import me.timeto.shared.vm.home.HomeMode

data class HomeBarUi(
    val homeMode: HomeMode,
    val taskFoldersUi: List<TaskFolderUi>,
    val noteFoldersUi: List<NoteFolderUi>,
)
