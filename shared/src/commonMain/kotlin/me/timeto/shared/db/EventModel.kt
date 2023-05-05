package timeto.shared.db

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dbsq.EventSQ
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import timeto.shared.*

data class EventModel(
    val id: Int,
    val text: String,
    val utc_time: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        suspend fun getAscByTime() = dbIO {
            db.eventQueries.getAscByTime().executeAsList().map { it.toModel() }
        }

        fun getAscByTimeFlow() = db.eventQueries.getAscByTime().asFlow()
            .mapToList().map { list -> list.map { it.toModel() } }

        suspend fun addWithValidation(
            text: String,
            localTime: Int,
            addToHistory: Boolean,
        ): Unit = dbIO {
            val newEventSQ = EventSQ(
                id = time(), // todo check unique
                text = validateText(text),
                utc_time = localTime + localUtcOffset
            )
            db.eventQueries.insertObject(newEventSQ)

            val newEvent = newEventSQ.toModel()
            if (addToHistory)
                EventsHistory.upsert(newEvent)
        }

        suspend fun syncTodaySafe(today: Int): Unit = dbIO {
            // Select within a transaction to avoid duplicate additions
            db.transaction {
                db.eventQueries.getAscByTime().executeAsList()
                    .map { it.toModel() }
                    .filter { it.getLocalTime().localDay <= today }
                    .sortedBy { event ->
                        // 00:00 - to the end
                        if (event.utc_time % 86_400 == 0)
                            return@sortedBy Int.MAX_VALUE
                        event.getLocalTime().time
                    }
                    .forEach { event ->
                        TaskModel.addWithValidation_transactionRequired(
                            event.prepTextForTask(),
                            DI.getTodayFolder()
                        )
                        db.eventQueries.deleteById(event.id)
                    }
            }
        }

        ///
        /// Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.eventQueries.getAscByTime().executeAsList().map { it.toModel() }

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
    ) = dbIO {
        val utcTime = localTime + localUtcOffset
        db.eventQueries.updateById(
            id = id, text = validateText(text), utc_time = utcTime
        )
    }

    suspend fun delete() = dbIO { db.eventQueries.deleteById(id) }

    ///
    /// Backupable Item

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

private fun EventSQ.toModel() = EventModel(
    id = id, text = text, utc_time = utc_time
)
