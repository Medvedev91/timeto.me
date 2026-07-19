package me.timeto.shared.vm.home

import me.timeto.shared.db.NoteFolderDb
import me.timeto.shared.db.TaskFolderDb

sealed class HomeModePrototype {

    data class TaskFolder(
        val taskFolderDb: TaskFolderDb,
    ) : HomeModePrototype()

    data class NoteFolder(
        val noteFolderDb: NoteFolderDb,
    ) : HomeModePrototype()
}
