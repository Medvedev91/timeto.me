package me.timeto.shared.db

import dbsq.EventTemplateSQ

data class EventTemplateDB(
    val id: Int,
    val sort: Int,
    val daytime: Int,
    val text: String,
) {

    companion object {

        suspend fun selectAscBySort(): List<EventTemplateDB> = dbIO {
            db.eventTemplateQueries.selectAscBySort().executeAsList().map { it.toDB() }
        }
    }
}

private fun EventTemplateSQ.toDB() = EventTemplateDB(
    id = id, sort = sort, daytime = daytime, text = text,
)
