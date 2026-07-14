package me.timeto.shared.db

import dbsq.NoteFolderSq
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.Symbol
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.getInt
import me.timeto.shared.getString
import me.timeto.shared.time
import me.timeto.shared.toJsonArray

data class NoteFolderDb(
    val id: Int,
    val time: Int,
    val sort: Int,
    val onHome: Boolean,
    val symbol_raw: String,
    val name: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        //
        // Select

        suspend fun selectAllSorted(): List<NoteFolderDb> = dbIo {
            db.noteFolderQueries.selectAllSorted().asList { toDb() }
        }

        fun selectAllSortedFlow(): Flow<List<NoteFolderDb>> =
            db.noteFolderQueries.selectAllSorted().asListFlow { toDb() }

        //
        // Insert

        suspend fun insertNoValidation(
            id: Int,
            sort: Int,
            onHome: Boolean,
            symbol: Symbol,
            name: String,
        ): Unit = dbIo {
            db.noteFolderQueries.insertWithId(
                NoteFolderSq(
                    id = id,
                    time = time(),
                    sort = sort,
                    on_home = if (onHome) 1 else 0,
                    symbol_raw = symbol.raw,
                    name = name,
                )
            )
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.noteFolderQueries.selectAllSorted().asList { toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.noteFolderQueries.insertWithId(
                NoteFolderSq(
                    id = j.getInt(0),
                    time = j.getInt(1),
                    sort = j.getInt(2),
                    on_home = j.getInt(3),
                    symbol_raw = j.getString(4),
                    name = j.getString(5),
                )
            )
        }
    }

    //
    // Backupable Item

    override fun backupable__getId(): String =
        id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, time, sort, onHome, symbol_raw, name,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.noteFolderQueries.updateById(
            id = j.getInt(0),
            time = j.getInt(1),
            sort = j.getInt(2),
            on_home = j.getInt(3),
            symbol_raw = j.getString(4),
            name = j.getString(5),
        )
    }

    override fun backupable__delete() {
        db.noteFolderQueries.deleteById(id)
    }
}

private fun NoteFolderSq.toDb() = NoteFolderDb(
    id = id,
    time = time,
    sort = sort,
    onHome = on_home != 0,
    symbol_raw = symbol_raw,
    name = name,
)
