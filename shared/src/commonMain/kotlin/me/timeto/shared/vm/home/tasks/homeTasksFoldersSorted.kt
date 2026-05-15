package me.timeto.shared.vm.home.tasks

import me.timeto.shared.TaskFolderUi

fun List<TaskFolderUi>.homeTasksFoldersSorted(): List<TaskFolderUi> = sortedBy {
    when {
        it.taskFolderDb.isToday -> -3
        it.taskFolderDb.isTomorrow -> -2
        it.taskFolderDb.isSomeday -> Int.MAX_VALUE
        else -> it.taskFolderDb.sort
    }
}
