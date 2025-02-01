package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dbsq.EventSQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*
import me.timeto.shared.misc.getInt
import me.timeto.shared.misc.getString
import me.timeto.shared.misc.toJsonArray

data class EventDb(
    val id: Int,
    val text: String,
    val utc_time: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        suspend fun getAscByTime() = dbIo {
            db.eventQueries.getAscByTime().executeAsList().map { it.toDb() }
        }

        fun getAscByTimeFlow() = db.eventQueries.getAscByTime().asFlow()
            .mapToList(Dispatchers.IO).map { list -> list.map { it.toDb() } }

        suspend fun addWithValidation(
            text: String,
            localTime: Int,
        ): Unit = dbIo {
            db.eventQueries.insertObject(
                EventSQ(
                    id = time(), // todo check unique
                    text = validateText(text),
                    utc_time = localTime + localUtcOffset
                )
            )
        }

        suspend fun syncTodaySafe(today: Int): Unit = dbIo {
            // Select within a transaction to avoid duplicate additions
            db.transaction {
                db.eventQueries.getAscByTime().executeAsList()
                    .map { it.toDb() }
                    .filter { it.getLocalTime().localDay <= today }
                    .sortedBy { event ->
                        // 00:00 - to the end
                        if (event.utc_time % 86_400 == 0)
                            return@sortedBy Int.MAX_VALUE
                        event.getLocalTime().time
                    }
                    .forEach { event ->
                        TaskDb.addWithValidation_transactionRequired(
                            event.prepTextForTask(),
                            Cache.getTodayFolderDb()
                        )
                        db.eventQueries.deleteById(event.id)
                    }
            }
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.eventQueries.getAscByTime().executeAsList().map { it.toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.eventQueries.insertObject(
                EventSQ(
                    id = j.getInt(0),
                    utc_time = j.getInt(1),
                    text = j.getString(2),
                )
            )
        }
    }

    fun prepTextForTask(): String = text
        .textFeatures()
        .copy(fromEvent = TextFeatures.FromEvent(getLocalTime()))
        .textWithFeatures()

    fun getLocalTime() = UnixTime(utc_time - localUtcOffset)

    suspend fun upWithValidation(
        text: String,
        localTime: Int,
    ) = dbIo {
        val utcTime = localTime + localUtcOffset
        db.eventQueries.updateById(
            id = id, text = validateText(text), utc_time = utcTime
        )
    }

    suspend fun delete() = dbIo { db.eventQueries.deleteById(id) }

    //
    // Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, utc_time, text,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.eventQueries.updateById(
            id = j.getInt(0),
            utc_time = j.getInt(1),
            text = j.getString(2),
        )
    }

    override fun backupable__delete() {
        db.eventQueries.deleteById(id)
    }
}

private fun validateText(text: String): String {
    val validatedText = text.trim()
    if (validatedText.isEmpty())
        throw UIException("Empty text")
    return validatedText
}

private fun EventSQ.toDb() = EventDb(
    id = id, text = text, utc_time = utc_time,
)
