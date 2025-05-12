package me.timeto.shared.ui.shortcuts

import kotlinx.coroutines.flow.MutableSharedFlow
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.launchExIo

object ShortcutPerformer {

    val flow = MutableSharedFlow<ShortcutDb>()

    fun perform(shortcutDb: ShortcutDb) {
        launchExIo {
            flow.emit(shortcutDb)
        }
    }
}

fun ShortcutDb.performUi() {
    ShortcutPerformer.perform(this)
}
