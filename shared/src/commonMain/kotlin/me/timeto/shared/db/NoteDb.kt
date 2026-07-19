package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.NoteSq
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.Cache
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.getInt
import me.timeto.shared.getString
import me.timeto.shared.time
import me.timeto.shared.toJsonArray
import me.timeto.shared.UiException
import kotlin.coroutines.cancellation.CancellationException

data class NoteDb(
    val id: Int,
    val time: Int,
    val sort: Int,
    val folderId: Int,
    val text: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        fun anyChangeFlow(): Flow<*> =
            db.noteQueries.anyChange().asFlow()

        suspend fun selectAllSorted(): List<NoteDb> = dbIo {
            db.noteQueries.selectAllSorted().asList { toDb() }
        }

        fun selectAllSortedFlow(): Flow<List<NoteDb>> =
            db.noteQueries.selectAllSorted().asListFlow { toDb() }

        @Throws(UiException::class, CancellationException::class)
        suspend fun insertWithValidation(
            text: String,
            noteFolderDb: NoteFolderDb,
        ): Unit = dbIo {
            db.transaction {
                db.noteQueries.insertAutoIncremented(
                    time = time(),
                    sort = 0,
                    folder_id = noteFolderDb.id,
                    text = validateText(text),
                )
            }
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.noteQueries.selectAllSorted().asList { toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.noteQueries.insertWithId(
                NoteSq(
                    id = j.getInt(0),
                    time = j.getInt(1),
                    sort = j.getInt(2),
                    folder_id = j.getInt(3),
                    text = j.getString(4),
                )
            )
        }
    }

    fun buildTitle(): String =
        "^(.*?)(\n|$)".toRegex().find(text)!!.value.trim()

    fun selectFolderDbCached(): NoteFolderDb =
        Cache.noteFoldersDb.first { it.id == folderId }

    @Throws(UiException::class, CancellationException::class)
    suspend fun updateWithValidation(
        newText: String,
        newNoteFolderDb: NoteFolderDb,
    ): Unit = dbIo {
        db.transaction {
            db.noteQueries.updateById(
                time = time,
                sort = sort,
                folder_id = newNoteFolderDb.id,
                text = validateText(newText),
                id = id,
            )
        }
    }

    suspend fun delete(): Unit = dbIo {
        db.noteQueries.deleteById(id)
    }

    //
    // Backupable Item

    override fun backupable__getId(): String =
        id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, time, sort, folderId, text,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.noteQueries.updateById(
            id = j.getInt(0),
            time = j.getInt(1),
            sort = j.getInt(2),
            folder_id = j.getInt(3),
            text = j.getString(4),
        )
    }

    override fun backupable__delete() {
        db.noteQueries.deleteById(id)
    }
}

///

@Throws(UiException::class)
private fun validateText(
    text: String,
): String {
    val resText = text.trim()
    if (resText.isEmpty())
        throw UiException("Empty text")
    return resText
}

///

private fun NoteSq.toDb() = NoteDb(
    id = id,
    time = time,
    sort = sort,
    folderId = folder_id,
    text = text,
)
