package timeto.shared.db

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dbsq.EventSQ
import kotlinx.coroutines.flow.map
import timeto.shared.*

data class EventModel(
    val id: Int,
    val text: String,
    val utc_time: Int,
) {

    companion object {

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
            val newEvent = addRaw(
                id = time(), // todo check unique
                text = validateText(text),
                utcTime = localTime + deviceUtcOffset
            )
            if (addToHistory)
                EventsHistory.upsert(newEvent)
        }

        fun addRaw(
            id: Int,
            text: String,
            utcTime: Int,
        ): EventModel {
            val newEventSQ = EventSQ(
                id = id, text = text, utc_time = utcTime
            )
            db.eventQueries.insertObject(newEventSQ)
            return newEventSQ.toModel()
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
                        val featureTime = TextFeatures.substringEvent(event.getLocalTime().time)
                        TaskModel.addWithValidation_transactionRequired(
                            "$EMOJI_CALENDAR ${event.timeToString()}\n${event.text} $featureTime",
                            DI.getTodayFolder()
                        )
                        db.eventQueries.deleteById(event.id)
                    }
            }
        }
    }

    fun getLocalTime() = UnixTime(utc_time - deviceUtcOffset)

    // todo is24
    fun timeToString(): String {
        val components = mutableListOf(
            UnixTime.StringComponent.dayOfMonth,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.month3,
            UnixTime.StringComponent.comma,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.dayOfWeek3,
        )
        if ((utc_time % 86_400) != 0) // If not 00:00
            components.addAll(
                listOf(
                    UnixTime.StringComponent.space,
                    UnixTime.StringComponent.hhmm24
                )
            )
        return getLocalTime().getStringByComponents(components)
    }

    suspend fun upWithValidation(
        text: String,
        localTime: Int,
    ) = dbIO {
        val utcTime = localTime + deviceUtcOffset
        db.eventQueries.updateById(
            id = id, text = validateText(text), utc_time = utcTime
        )
    }

    suspend fun delete() = dbIO { db.eventQueries.deleteById(id) }
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
