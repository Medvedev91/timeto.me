package me.timeto.shared.vm.home.bar

import kotlinx.coroutines.flow.MutableSharedFlow

sealed class HomeBarAnimate {

    companion object {

        val flow = MutableSharedFlow<HomeBarAnimate>()
    }

    data class TaskFolder(
        val taskFolderId: Int,
    ) : HomeBarAnimate()

    data class NoteFolder(
        val noteFolderId: Int,
    ) : HomeBarAnimate()
}
