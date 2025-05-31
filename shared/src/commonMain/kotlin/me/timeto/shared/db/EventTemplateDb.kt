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
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.getInt
import me.timeto.shared.getString
import me.timeto.shared.time
import me.timeto.shared.toJsonArray
import me.timeto.shared.UiException

data class EventTemplateDb(
    val id: Int,
    val sort: Int,
    val daytime: Int,
    val text: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        suspend fun selectAscSorted(): List<EventTemplateDb> = dbIo {
            db.eventTemplateQueries.selectAscSorted().executeAsList().toDbList()
        }

        fun selectAscSortedFlow(): Flow<List<EventTemplateDb>> = db.eventTemplateQueries
            .selectAscSorted().asFlow().mapToList(Dispatchers.IO).map { it.toDbList() }

        suspend fun insertWithValidation(
            daytime: Int,
            text: String,
        ) {
            dbIo {
                val templates = db.eventTemplateQueries.selectAscSorted().executeAsList().toDbList()
                db.eventTemplateQueries.insertObject(
                    EventTemplateSQ(
                        id = time(), // todo validation
                        sort = 0,
                        daytime = dayTimeValidation(daytime),
                        text = textValidation(text, templates),
                    )
                )
            }
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.eventTemplateQueries.selectAscSorted().executeAsList().toDbList()

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

    suspend fun updateWithValidation(
        daytime: Int,
        text: String,
    ) {
        dbIo {
            db.transaction {
                val templates = db.eventTemplateQueries
                    .selectAscSorted()
                    .executeAsList()
                    .toDbList()
                    .filter { id != it.id }
                db.eventTemplateQueries.updateById(
                    id = id,
                    sort = sort,
                    daytime = dayTimeValidation(daytime),
                    text = textValidation(text, templates),
                )
            }
        }
    }

    //
    // Backupable Item

    override fun backupable__getId(): String =
        id.toString()

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

private fun EventTemplateSQ.toDb() = EventTemplateDb(
    id = id, sort = sort, daytime = daytime, text = text,
)

private fun List<EventTemplateSQ>.toDbList(): List<EventTemplateDb> =
    this.map { it.toDb() }

private fun dayTimeValidation(daytime: Int): Int {
    if (daytime < 0)
        throw UiException("Invalid daytime")
    return daytime
}

private fun textValidation(
    text: String,
    otherTemplates: List<EventTemplateDb>,
): String {
    val textValidated = text.trim()
    if (otherTemplates.any { it.text == textValidated })
        throw UiException("Template \"$textValidated\" already exists")
    return textValidated
}
