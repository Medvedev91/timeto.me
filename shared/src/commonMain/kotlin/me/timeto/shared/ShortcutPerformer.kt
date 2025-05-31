package me.timeto.shared

import kotlinx.coroutines.flow.MutableSharedFlow
import me.timeto.shared.db.ShortcutDb

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
