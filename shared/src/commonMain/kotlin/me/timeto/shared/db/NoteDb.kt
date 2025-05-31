package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.NoteSQ
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
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
    val sort: Int,
    val text: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        fun anyChangeFlow(): Flow<*> =
            db.noteQueries.anyChange().asFlow()

        suspend fun selectAsc(): List<NoteDb> = dbIo {
            db.noteQueries.selectAsc().asList { toDb() }
        }

        fun selectAscFlow(): Flow<List<NoteDb>> =
            db.noteQueries.selectAsc().asListFlow { toDb() }

        @Throws(UiException::class, CancellationException::class)
        suspend fun insertWithValidation(
            text: String,
        ): Unit = dbIo {
            db.transaction {
                val nextId = time() // todo
                db.noteQueries.insert(
                    NoteSQ(
                        id = nextId,
                        sort = 0,
                        text = validateText(text),
                    )
                )
            }
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.noteQueries.selectAsc().asList { toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.noteQueries.insert(
                NoteSQ(
                    id = j.getInt(0),
                    sort = j.getInt(1),
                    text = j.getString(2),
                )
            )
        }
    }

    val title: String by lazy {
        "^(.*?)(\n|$)".toRegex().find(text)!!.value.trim()
    }

    @Throws(UiException::class, CancellationException::class)
    suspend fun updateWithValidation(
        newText: String,
    ): Unit = dbIo {
        db.transaction {
            db.noteQueries.updateById(
                id = id,
                sort = sort,
                text = validateText(newText),
            )
        }
    }

    suspend fun delete(): Unit = dbIo {
        db.noteQueries.deleteById(id)
    }

    //
    // Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, sort, text,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.noteQueries.updateById(
            id = j.getInt(0),
            sort = j.getInt(1),
            text = j.getString(2),
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

private fun NoteSQ.toDb() = NoteDb(
    id = id, sort = sort, text = text,
)
