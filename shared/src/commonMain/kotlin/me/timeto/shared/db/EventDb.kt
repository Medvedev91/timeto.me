package me.timeto.shared.db

import dbsq.EventSQ
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.misc.getInt
import me.timeto.shared.misc.getString
import me.timeto.shared.time
import me.timeto.shared.misc.toJsonArray
import me.timeto.shared.ui.UiException

data class EventDb(
    val id: Int,
    val text: String,
    val utc_time: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        suspend fun selectAscByTime(): List<EventDb> = dbIo {
            db.eventQueries.selectAscByTime().asList { toDb() }
        }

        fun selectAscByTimeFlow(): Flow<List<EventDb>> =
            db.eventQueries.selectAscByTime().asListFlow { toDb() }

        suspend fun insertWithValidation(
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
                db.eventQueries.selectAscByTime().executeAsList()
                    .map { it.toDb() }
                    .filter { it.getLocalTime().localDay <= today }
                    .sortedBy { event ->
                        // 00:00 - to the end
                        if (event.utc_time % 86_400 == 0)
                            return@sortedBy Int.MAX_VALUE
                        event.getLocalTime().time
                    }
                    .forEach { event ->
                        TaskDb.insertWithValidation_transactionRequired(
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
            db.eventQueries.selectAscByTime().asList { toDb() }

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

    fun getLocalTime() =
        UnixTime(utc_time - localUtcOffset)

    suspend fun updateWithValidation(
        text: String,
        localTime: Int,
    ): Unit = dbIo {
        val utcTime = localTime + localUtcOffset
        db.eventQueries.updateById(
            id = id, text = validateText(text), utc_time = utcTime
        )
    }

    suspend fun delete(): Unit =
        dbIo { db.eventQueries.deleteById(id) }

    //
    // Backupable Item

    override fun backupable__getId(): String =
        id.toString()

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

@Throws(UiException::class)
private fun validateText(text: String): String {
    val validatedText = text.trim()
    if (validatedText.isEmpty())
        throw UiException("Empty text")
    return validatedText
}

private fun EventSQ.toDb() = EventDb(
    id = id, text = text, utc_time = utc_time,
)
