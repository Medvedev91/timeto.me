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

data class NoteModel(
    val id: Int,
    val sort: Int,
    val text: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        fun anyChangeFlow() = db.noteQueries.anyChange().asFlow()

        suspend fun getAsc(): List<NoteModel> = dbIO {
            db.noteQueries.getAsc().toModels()
        }

        fun getAscFlow(): Flow<List<NoteModel>> = db.noteQueries.getAsc().asFlow()
            .mapToList(Dispatchers.IO).map { list -> list.map { it.toModel() } }

        suspend fun addWithValidation(
            text: String,
        ) = dbIO {
            db.transaction {
                val nextId = time() // todo
                val otherNotes = db.noteQueries.getAsc().toModels()
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
            db.noteQueries.getAsc().toModels()

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

    suspend fun upWithValidation(
        newText: String,
    ): Unit = dbIO {
        db.transaction {
            val otherNotes = db.noteQueries.getAsc().toModels()
                .filter { it.id != id }
            db.noteQueries.upById(
                id = id,
                sort = sort,
                text = textValidation(newText, otherNotes = otherNotes)
            )
        }
    }

    suspend fun delete(): Unit = dbIO {
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

private fun NoteSQ.toModel() = NoteModel(
    id = id, sort = sort, text = text
)

private fun Query<NoteSQ>.toModels(): List<NoteModel> =
    executeAsList().map { it.toModel() }

///

private fun textValidation(
    text: String,
    otherNotes: List<NoteModel>,
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
