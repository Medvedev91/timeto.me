package me.timeto.shared.vm.home.tasks

import me.timeto.shared.DialogsManager
import me.timeto.shared.NoteFolderUi
import me.timeto.shared.UiException
import me.timeto.shared.db.NoteDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.launchExIo
import me.timeto.shared.textFeatures

// STA - Swipe to Action
data class HomeTaskStaNoteFolderUi(
    val taskDb: TaskDb,
    val noteFolderUi: NoteFolderUi,
) {

    fun onTap(
        dialogsManager: DialogsManager,
    ) {
        launchExIo {
            try {
                NoteDb.insertWithValidation(
                    text = taskDb.text.textFeatures().textNoFeatures,
                    noteFolderDb = noteFolderUi.noteFolderDb,
                )
                taskDb.delete()
            } catch (e: UiException) {
                dialogsManager.alert(e.uiMessage)
            }
        }
    }
}
