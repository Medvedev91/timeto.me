package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dbsq.EventTemplateSQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class EventTemplateDB(
    val id: Int,
    val sort: Int,
    val daytime: Int,
    val text: String,
) {

    companion object {

        suspend fun selectAscSorted(): List<EventTemplateDB> = dbIO {
            db.eventTemplateQueries.selectAscSorted().executeAsList().map { it.toDB() }
        }

        fun selectAscSortedFlow(): Flow<List<EventTemplateDB>> = db.eventTemplateQueries
            .selectAscSorted().asFlow().mapToList(Dispatchers.IO).map { list -> list.map { it.toDB() } }
    }
}

private fun EventTemplateSQ.toDB() = EventTemplateDB(
    id = id, sort = sort, daytime = daytime, text = text,
)
