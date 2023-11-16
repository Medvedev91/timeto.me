package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dbsq.EventTemplateSQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*

data class EventTemplateDB(
    val id: Int,
    val sort: Int,
    val daytime: Int,
    val text: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        suspend fun selectAscSorted(): List<EventTemplateDB> = dbIO {
            db.eventTemplateQueries.selectAscSorted().executeAsList().map { it.toDB() }
        }

        fun selectAscSortedFlow(): Flow<List<EventTemplateDB>> = db.eventTemplateQueries
            .selectAscSorted().asFlow().mapToList(Dispatchers.IO).map { list -> list.map { it.toDB() } }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.eventTemplateQueries.selectAscSorted().executeAsList().map { it.toDB() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.eventTemplateQueries.insertObject(
                EventTemplateSQ(
                    id = j.getInt(0),
                    sort = j.getInt(1),
                    daytime = j.getInt(2),
                    text = j.getString(3),
                )
            )
        }
    }

    //
    // Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, sort, daytime, text,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.eventTemplateQueries.updateById(
            id = j.getInt(0),
            sort = j.getInt(1),
            daytime = j.getInt(2),
            text = j.getString(3),
        )
    }

    override fun backupable__delete() {
        db.eventTemplateQueries.deleteById(id)
    }
}

private fun EventTemplateSQ.toDB() = EventTemplateDB(
    id = id, sort = sort, daytime = daytime, text = text,
)
