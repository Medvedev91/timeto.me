package me.timeto.shared.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dbsq.NoteSQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*

data class NoteDb(
    val id: Int,
    val sort: Int,
    val text: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        fun anyChangeFlow() = db.noteQueries.anyChange().asFlow()

        suspend fun getAsc(): List<NoteDb> = dbIo {
            db.noteQueries.getAsc().toDbList()
        }

        fun getAscFlow(): Flow<List<NoteDb>> = db.noteQueries.getAsc().asFlow()
            .mapToList(Dispatchers.IO).map { list -> list.map { it.toDb() } }

        suspend fun addWithValidation(
            text: String,
        ) = dbIo {
            db.transaction {
                val nextId = time() // todo
                val otherNotes = db.noteQueries.getAsc().toDbList()
                db.noteQueries.insert(
                    NoteSQ(
                        id = nextId,
                        sort = 0,
                        text = textValidation(text, otherNotes = otherNotes),
                    )
                )
            }
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.noteQueries.getAsc().toDbList()

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

    suspend fun upWithValidation(
        newText: String,
    ): Unit = dbIo {
        db.transaction {
            val otherNotes = db.noteQueries.getAsc().toDbList()
                .filter { it.id != id }
            db.noteQueries.upById(
                id = id,
                sort = sort,
                text = textValidation(newText, otherNotes = otherNotes)
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
        db.noteQueries.upById(
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

private fun NoteSQ.toDb() = NoteDb(
    id = id, sort = sort, text = text
)

private fun Query<NoteSQ>.toDbList(): List<NoteDb> =
    executeAsList().map { it.toDb() }

///

private fun textValidation(
    text: String,
    otherNotes: List<NoteDb>,
): String {

    val resText = text.trim()
    if (resText.isEmpty())
        throw UIException("Empty text")

    otherNotes.forEach { note ->
        if (note.text.equals(resText, ignoreCase = true))
            throw UIException("$resText already exists.")
    }

    return resText
}
