package me.timeto.shared

import me.timeto.shared.db.NoteFolderDb

data class NoteFolderUi(
    val noteFolderDb: NoteFolderDb,
) {
    val symbol: Symbol =
        noteFolderDb.symbolOrDefault()
}
